package com.cn.hzm.server.util;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.domain.fulfilment.ShipmentMember;
import com.cn.hzm.core.aws.domain.inventory.DetailMember;
import com.cn.hzm.core.aws.domain.inventory.Member;
import com.cn.hzm.core.aws.domain.order.Order;
import com.cn.hzm.core.aws.domain.order.OrderItem;
import com.cn.hzm.core.aws.domain.product.*;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.aws.resp.product.GetMyPriceForSkuResponse;
import com.cn.hzm.core.entity.*;
import com.cn.hzm.core.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.ParseException;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:00 下午
 */
@Slf4j
public class ConvertUtil {


    public static ItemDO convertToItemDO(ItemDO itemDO, GetMatchingProductForIdResponse resp, String sku) {
        Product product = resp.getGetMatchingProductForIdResult().getProducts().getList().get(0);
        MarketplaceASIN marketplaceASIN = product.getIdentifiers().getMarketplaceASIN();
        itemDO.setAsin(marketplaceASIN.getAsin());
        itemDO.setMarketplaceId(marketplaceASIN.getMarketplaceId());

        ItemAttributes itemAttributes = product.getAttributeSets().getItemAttributes();
        itemDO.setSku(!StringUtils.isEmpty(sku) ? sku : itemAttributes.getModel());
        itemDO.setTitle(itemAttributes.getTitle());
        itemDO.setIcon(itemAttributes.getSmallImage().getUrl());

        //添加尺寸
        itemDO.setPackageDimension(JSONObject.toJSONString(itemAttributes.getPackageDimensions()));

        //添加商品类型
        itemDO.setItemType(itemAttributes.getProductTypeName());

        //添加排名
        if (product.getSalesRankings() != null) {
            itemDO.setSaleRank(JSONObject.toJSONString(product.getSalesRankings()));
        }

        itemDO.setAttributeSet(JSONObject.toJSONString(product.getAttributeSets()));

        itemDO.setRelationship(JSONObject.toJSONString(product.getRelationships()));

        //0:子体，1：父体，2：即没有子体也没有父体
        if (product.getRelationships().getVariationParent() != null) {
            itemDO.setIsParent(0);
        } else if (product.getRelationships().getVariationChildrens() != null) {
            itemDO.setIsParent(1);
        } else {
            itemDO.setIsParent(2);
        }
        return itemDO;
    }

    public static Double getItemPrice(GetMyPriceForSkuResponse priceResp) {
        Double itemPrice = 0.0;
        if (priceResp != null && priceResp.getGetMyPriceForSkuResult() != null && priceResp.getGetMyPriceForSkuResult().getProduct() != null) {
            Product product = priceResp.getGetMyPriceForSkuResult().getProduct();

            if (product.getOffers() != null && product.getOffers().getOffer() != null) {
                Offer offer = priceResp.getGetMyPriceForSkuResult().getProduct().getOffers().getOffer();
                if (offer.getRegularPrice() != null) {
                    itemPrice = offer.getRegularPrice().getAmount();
                } else {
                    if (offer.getBuyingPrice() != null) {
                        BuyingPrice buyingPrice = offer.getBuyingPrice();
                        if (buyingPrice.getLandedPrice() != null) {
                            itemPrice = buyingPrice.getLandedPrice().getAmount();
                        } else if (buyingPrice.getListingPrice() != null) {
                            itemPrice = buyingPrice.getListingPrice().getAmount();
                        } else {
                            itemPrice = buyingPrice.getShipping().getAmount();
                        }
                    }
                }
            }
        }
        return itemPrice;
    }


    public static void convertToInventoryDO(ListInventorySupplyResponse inventory, InventoryDO inventoryDO) {
        Member member = inventory.getListInventorySupplyResult().getInventorySupplyList().getMembers().get(0);
        inventoryDO.setSku(member.getSellerSKU());
        inventoryDO.setAsin(member.getAsin() == null ? "" : member.getAsin());
        inventoryDO.setFnsku(member.getFnsku() == null ? "" : member.getFnsku());
        inventoryDO.setItemCondition(member.getCondition());
        inventoryDO.setEarliestAvailability(JSONObject.toJSONString(member.getEarliestAvailability()));
        inventoryDO.setAmazonQuantity(member.getTotalSupplyQuantity() == null ? 0 : member.getTotalSupplyQuantity());
        inventoryDO.setAmazonStockQuantity(member.getInStockSupplyQuantity() == null ? 0 : member.getInStockSupplyQuantity());

        Integer transferNum = 0;
        Integer inboundNum = 0;
        if (member.getSupplyDetail() != null && member.getSupplyDetail().getMembers() != null) {
            for (DetailMember detailMember : member.getSupplyDetail().getMembers()) {
                if (detailMember.getSupplyType().equals("Inbound")) {
                    inboundNum += detailMember.getQuantity();
                } else if (detailMember.getSupplyType().equals("Transfer")) {
                    transferNum += detailMember.getQuantity();
                }
            }
            inventoryDO.setSupplyDetail(JSONObject.toJSONString(member.getSupplyDetail()));
        }

        inventoryDO.setAmazonTransferQuantity(transferNum);
        inventoryDO.setAmazonInboundQuantity(inboundNum);

        //设置初始值
        if (inventoryDO.getId() == null) {
            inventoryDO.setLocalQuantity(0);
        }
        inventoryDO.calculateTotalQuantity();
    }

    public static OrderDO convertToOrderDO(OrderDO orderDO, Order order) throws ParseException {
        orderDO.setAmazonOrderId(order.getAmazonOrderId());
        orderDO.setSellerOrderId(order.getSellerOrderId());
        orderDO.setPurchaseDate(TimeUtil.transform(order.getPurchaseDate()));
        orderDO.setLastUpdateDate(TimeUtil.transform(order.getLastUpdateDate()));
        orderDO.setOrderStatus(order.getOrderStatus());
        orderDO.setFulfillmentChannel(order.getFulfillmentChannel());
        orderDO.setSalesChannel(order.getSalesChannel());
        orderDO.setOrderChannel(order.getOrderChannel());
        orderDO.setShipServiceLevel(order.getShipServiceLevel());
        orderDO.setShippingAddress(JSONObject.toJSONString(order.getShippingAddress()));
        if (order.getOrderTotal() != null) {
            orderDO.setOrderAmount(Double.parseDouble(order.getOrderTotal().getAmount()));
            orderDO.setOrderCurrencyCode(order.getOrderTotal().getCurrencyCode());
        }
        orderDO.setNumberOfItemsShipped(Integer.valueOf(order.getNumberOfItemsShipped()));
        orderDO.setNumberOfItemsUnshipped(Integer.valueOf(order.getNumberOfItemsUnshipped()));
        orderDO.setPaymentMethodDetails(JSONObject.toJSONString(order.getPaymentMethodDetails()));
        orderDO.setPaymentMethod(order.getPaymentMethod());
        orderDO.setMarketplaceId(order.getMarketplaceId());
        orderDO.setBuyerEmail(order.getBuyerEmail());
        orderDO.setBuyerName(order.getBuyerName());
        orderDO.setShipmentServiceLevelCategory(order.getShipmentServiceLevelCategory());
        orderDO.setShippedByAmazonTFM(Boolean.parseBoolean(order.getShippedByAmazonTFM()) ? 1 : 0);
        orderDO.setTfmShipmentStatus(order.getTfmShipmentStatus());
        orderDO.setCbaDisplayableShippingLabel(order.getCbaDisplayableShippingLabel());
        orderDO.setOrderType(order.getOrderType());
        orderDO.setEarliestShipDate(order.getEarliestShipDate() != null ? TimeUtil.transformUTCToDate(order.getEarliestShipDate()) : null);
        orderDO.setLatestShipDate(order.getLatestShipDate() != null ? TimeUtil.transformUTCToDate(order.getLatestShipDate()) : null);
        orderDO.setEarliestDeliveryDate(order.getEarliestDeliveryDate() != null ? TimeUtil.transformUTCToDate(order.getEarliestDeliveryDate()) : null);
        orderDO.setLatestDeliveryDate(order.getLatestDeliveryDate() != null ? TimeUtil.transformUTCToDate(order.getLatestDeliveryDate()) : null);
        orderDO.setIsReplacementOrder(Boolean.parseBoolean(order.getIsReplacementOrder()) ? 1 : 0);
        orderDO.setIsBusinessOrder(Boolean.parseBoolean(order.getIsBusinessOrder()) ? 1 : 0);
        orderDO.setIsGlobalExpressEnabled(Boolean.parseBoolean(order.getIsGlobalExpressEnabled()) ? 1 : 0);
        orderDO.setIsSoldByAB(Boolean.parseBoolean(order.getIsSoldByAB()) ? 1 : 0);
        orderDO.setIsPremiumOrder(Boolean.parseBoolean(order.getIsPremiumOrder()) ? 1 : 0);
        orderDO.setIsISPU(Boolean.parseBoolean(order.getIsISPU()) ? 1 : 0);
        orderDO.setIsPrime(Boolean.parseBoolean(order.getIsPrime()) ? 1 : 0);
        return orderDO;
    }

    public static OrderItemDO convertToOrderItemDO(OrderItemDO orderItemDO, OrderItem orderItem, String amazonOrderId) throws ParseException {
        orderItemDO.setAmazonOrderId(amazonOrderId);
        orderItemDO.setAsin(orderItem.getAsin());
        orderItemDO.setSku(orderItem.getSellerSKU());
        orderItemDO.setOrderItemId(orderItem.getOrderItemId());
        orderItemDO.setTitle(orderItem.getTitle());
        orderItemDO.setQuantityOrdered(Integer.valueOf(orderItem.getQuantityOrdered()));
        orderItemDO.setQuantityShipped(Integer.valueOf(orderItem.getQuantityShipped()));

        if (orderItem.getItemPrice() != null) {
            orderItemDO.setItemPriceAmount(orderItem.getItemPrice().getAmount());
            orderItemDO.setItemPriceCurrencyCode(orderItem.getItemPrice().getCurrencyCode());
        }
        if (orderItem.getItemTax() != null) {
            orderItemDO.setItemTaxAmount(orderItem.getItemTax().getAmount());
            orderItemDO.setItemTaxCurrencyCode(orderItem.getItemTax().getCurrencyCode());
        }

        if (orderItem.getShippingPrice() != null) {
            orderItemDO.setShippingPriceAmount(orderItem.getShippingPrice().getAmount());
            orderItemDO.setShippingPriceCurrencyCode(orderItem.getShippingPrice().getCurrencyCode());
        }
        if (orderItem.getShippingTax() != null) {
            orderItemDO.setShippingTaxAmount(orderItem.getShippingTax().getAmount());
            orderItemDO.setShippingTaxCurrencyCode(orderItem.getShippingTax().getCurrencyCode());
        }

        if (orderItem.getShippingDiscount() != null) {
            orderItemDO.setShippingDiscountAmount(orderItem.getShippingDiscount().getAmount());
            orderItemDO.setShippingDiscountCurrencyCode(orderItem.getShippingDiscount().getCurrencyCode());
        }
        if (orderItem.getShippingDiscountTax() != null) {
            orderItemDO.setShippingDiscountTaxAmount(orderItem.getShippingDiscountTax().getAmount());
            orderItemDO.setShippingDiscountTaxCurrencyCode(orderItem.getShippingDiscountTax().getCurrencyCode());
        }

        if (orderItem.getGiftWrapPrice() != null) {
            orderItemDO.setGiftWrapPriceAmount(orderItem.getGiftWrapPrice().getAmount());
            orderItemDO.setGiftWrapPriceCurrencyCode(orderItem.getGiftWrapPrice().getCurrencyCode());
        }
        if (orderItem.getGiftWrapTax() != null) {
            orderItemDO.setGiftWrapTaxAmount(orderItem.getGiftWrapTax().getAmount());
            orderItemDO.setGiftWrapTaxCurrencyCode(orderItem.getGiftWrapTax().getCurrencyCode());
        }

        if (orderItem.getPromotionDiscount() != null) {
            orderItemDO.setPromotionDiscountAmount(orderItem.getPromotionDiscount().getAmount());
            orderItemDO.setPromotionDiscountCurrencyCode(orderItem.getPromotionDiscount().getCurrencyCode());
        }
        if (orderItem.getPromotionDiscountTax() != null) {
            orderItemDO.setPromotionDiscountTaxAmount(orderItem.getPromotionDiscountTax().getAmount());
            orderItemDO.setPromotionDiscountTaxCurrencyCode(orderItem.getPromotionDiscountTax().getCurrencyCode());
        }

        orderItemDO.setPromotionIds(JSONObject.toJSONString(orderItem.getPromotionIds()));

        if (orderItem.getCodFee() != null) {
            orderItemDO.setCodFeeAmount(orderItem.getCodFee().getAmount());
            orderItemDO.setCodFeeCurrencyCode(orderItem.getCodFee().getCurrencyCode());
        }
        if (orderItem.getCodFeeDiscount() != null) {
            orderItemDO.setCodFeeDiscountAmount(orderItem.getCodFeeDiscount().getAmount());
            orderItemDO.setCodFeeDiscountCurrencyCode(orderItem.getCodFeeDiscount().getCurrencyCode());
        }

        orderItemDO.setGiftMessageText(orderItem.getGiftMessageText());
        orderItemDO.setGiftWrapLevel(orderItem.getGiftWrapLevel());
        orderItemDO.setInvoiceData(JSONObject.toJSONString(orderItem.getInvoiceData()));
        orderItemDO.setConditionNote(orderItem.getConditionNote());
        orderItemDO.setConditionId(orderItem.getConditionId());
        orderItemDO.setConditionSubtypeId(orderItem.getConditionSubtypeId());

        orderItemDO.setScheduledDeliveryStartDate(orderItem.getScheduledDeliveryStartDate() != null ? TimeUtil.transform(orderItem.getScheduledDeliveryStartDate()) : null);
        orderItemDO.setScheduledDeliveryEndDate(orderItem.getScheduledDeliveryEndDate() != null ? TimeUtil.transform(orderItem.getScheduledDeliveryEndDate()) : null);

        orderItemDO.setTaxCollection(JSONObject.toJSONString(orderItem.getTaxCollection()));
        orderItemDO.setProductInfo(JSONObject.toJSONString(orderItem.getProductInfo()));
        orderItemDO.setIsGift(Boolean.parseBoolean(orderItem.getIsGift()) ? 1 : 0);
        orderItemDO.setIsTransparency(Boolean.parseBoolean(orderItem.getIsTransparency()) ? 1 : 0);

        return orderItemDO;
    }

    public static ShipmentInfoRecordDO convertToShipmentInfoDO(ShipmentInfoRecordDO shipmentInfo, ShipmentMember shipmentMember) {
        shipmentInfo.setFcid(shipmentMember.getDestinationFulfillmentCenterId());
        shipmentInfo.setLpType(shipmentMember.getLabelPrepType());
        shipmentInfo.setShipAddress(JSONObject.toJSONString(shipmentMember.getShipFromAddress()));
        shipmentInfo.setShipmentId(shipmentMember.getShipmentId());
        shipmentInfo.setShipmentName(shipmentMember.getShipmentName());
        shipmentInfo.setBoxContentsSource(shipmentMember.getBoxContentsSource() == null ? "" : shipmentMember.getBoxContentsSource());
        shipmentInfo.setShipmentStatus(shipmentMember.getShipmentStatus());
        return shipmentInfo;
    }

    public static ShipmentItemRecordDO convertToShipmentItemDO(ShipmentItemRecordDO shipmentItemDO, com.cn.hzm.core.aws.domain.fulfilment.Member member) {
        shipmentItemDO.setQuantityShipped(member.getQuantityShipped());
        shipmentItemDO.setShipmentId(member.getShipmentId());
        shipmentItemDO.setPrepDetailsList(JSONObject.toJSONString(member.getPrepDetailsList()));
        shipmentItemDO.setFulfillmentNetworkSKU(member.getFulfillmentNetworkSKU());
        shipmentItemDO.setSellerSKU(member.getSellerSKU());
        shipmentItemDO.setQuantityReceived(member.getQuantityReceived());
        shipmentItemDO.setQuantityInCase(member.getQuantityInCase());
        return shipmentItemDO;
    }
}
