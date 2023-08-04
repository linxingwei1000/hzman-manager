package com.cn.hzm.core.util;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.spa.StringUtil;
import com.cn.hzm.core.spa.fbainbound.model.InboundShipmentInfo;
import com.cn.hzm.core.spa.fbainbound.model.InboundShipmentItem;
import com.cn.hzm.core.spa.fbainventory.model.GetInventorySummariesResponse;
import com.cn.hzm.core.spa.fbainventory.model.InventoryDetails;
import com.cn.hzm.core.spa.fbainventory.model.InventorySummary;
import com.cn.hzm.core.spa.item.model.*;
import com.cn.hzm.core.spa.order.model.Order;
import com.cn.hzm.core.spa.order.model.OrderItem;
import com.cn.hzm.core.spa.price.model.*;
import com.cn.hzm.core.spa.price.model.Product;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 12:20 下午
 */
public class ConvertUtil {

    public static ItemDo convertToItemDo(ItemDo itemDO, Item item, String sku, AwsUserMarketDo awsUserMarketDo) {
        itemDO.setUserMarketId(awsUserMarketDo.getId());
        itemDO.setAsin(item.getAsin());
        itemDO.setMarketplaceId(awsUserMarketDo.getMarketId());

        itemDO.setActive(1);
        itemDO.setItemCost(0.0);


        ItemSummaryByMarketplace itemSummary = null;
        for (ItemSummaryByMarketplace tmp : item.getSummaries()) {
            //获取同个市场图标
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemSummary = tmp;
                break;
            }
        }

        itemDO.setTitle(itemSummary != null ? itemSummary.getItemName() : "");
        //todo 再看一下返回
        itemDO.setSku(StringUtil.isEmpty(sku) ? itemSummary.getModelNumber() : sku);

        ItemImagesByMarketplace itemImage = null;
        for (ItemImagesByMarketplace tmp : item.getImages()) {
            //获取同个市场图标
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemImage = tmp;
                break;
            }
        }

        String iconUrl = "";
        if (itemImage != null) {
            int size = 0;
            for (ItemImage tmp : itemImage.getImages()) {
                if (tmp.getVariant().equals(ItemImage.VariantEnum.MAIN) && tmp.getHeight() > size) {
                    iconUrl = tmp.getLink();
                }
            }
        }
        itemDO.setIcon(iconUrl);

        //添加尺寸
        ItemDimensionsByMarketplace itemDimensions = null;
        for (ItemDimensionsByMarketplace tmp : item.getDimensions()) {
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemDimensions = tmp;
                break;
            }
        }
        itemDO.setPackageDimension(itemDimensions != null ? JSONObject.toJSONString(itemDimensions) : "");

        //添加商品类型
        ItemProductTypeByMarketplace itemProduct = null;
        for (ItemProductTypeByMarketplace tmp : item.getProductTypes()) {
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemProduct = tmp;
                break;
            }
        }
        itemDO.setItemType(itemProduct != null ? itemProduct.getProductType() : "");

        //添加排名
        ItemSalesRanksByMarketplace itemSalesRanks = null;
        for (ItemSalesRanksByMarketplace tmp : item.getSalesRanks()) {
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemSalesRanks = tmp;
                break;
            }
        }
        itemDO.setSaleRank(itemSalesRanks != null ? JSONObject.toJSONString(itemSalesRanks) : "");

        //添加特征, 从item.getAttributes()看了一下，就是{}，空的
        itemDO.setAttributeSet("");

        ItemRelationshipsByMarketplace itemRelationships = null;
        for (ItemRelationshipsByMarketplace tmp : item.getRelationships()) {
            if (tmp.getMarketplaceId().equals(awsUserMarketDo.getMarketId())) {
                itemRelationships = tmp;
                break;
            }
        }
        itemDO.setRelationship(itemRelationships != null ? JSONObject.toJSONString(itemRelationships) : "");

        //0:子体，1：父体，2：即没有子体也没有父体
        if (CollectionUtils.isEmpty(itemRelationships.getRelationships())) {
            itemDO.setIsParent(2);
        } else {
            ItemRelationship itemRelationship = itemRelationships.getRelationships().get(0);
            if (itemRelationship.getParentAsins() != null) {
                itemDO.setIsParent(0);
            } else if (itemRelationship.getChildAsins() != null) {
                itemDO.setIsParent(1);
            } else {
                itemDO.setIsParent(2);
            }
        }
        return itemDO;
    }

    public static void addListingTime(ItemDo itemDO, com.cn.hzm.core.spa.listings.model.Item listItem) {
        for (com.cn.hzm.core.spa.listings.model.ItemSummaryByMarketplace itemSummaryByMarketplace : listItem.getSummaries()) {
            if (itemSummaryByMarketplace.getMarketplaceId().equals(itemDO.getMarketplaceId())) {
                itemDO.setListingTime(TimeUtil.transformOffsetToDate(itemSummaryByMarketplace.getCreatedDate()));
                break;
            }
        }
    }

    public static Double getItemPrice(GetPricingResponse priceResp) {
        Double itemPrice = 0.0;
        if (priceResp == null) {
            return itemPrice;
        }

        if (priceResp != null && priceResp.getPayload() != null) {
            Price price = priceResp.getPayload().get(0);

            Product product = price.getProduct();
            if (product.getOffers() != null) {
                OfferType offer = product.getOffers().get(0);
                if (offer.getRegularPrice() != null) {
                    itemPrice = offer.getRegularPrice().getAmount().doubleValue();
                } else {
                    if (offer.getBuyingPrice() != null) {
                        PriceType priceType = offer.getBuyingPrice();
                        if (priceType.getLandedPrice() != null) {
                            itemPrice = priceType.getLandedPrice().getAmount().doubleValue();
                        } else if (priceType.getListingPrice() != null) {
                            itemPrice = priceType.getListingPrice().getAmount().doubleValue();
                        } else {
                            itemPrice = priceType.getShipping().getAmount().doubleValue();
                        }
                    }
                }
            }
        }
        return itemPrice;
    }


    public static void convertToInventoryDO(GetInventorySummariesResponse inventory, ItemInventoryDo itemInventoryDo, Integer userMarketId) {
        InventorySummary summary = inventory.getPayload().getInventorySummaries().get(0);
        itemInventoryDo.setSku(summary.getSellerSku());
        itemInventoryDo.setAsin(summary.getAsin() == null ? "" : summary.getAsin());
        itemInventoryDo.setFnsku(summary.getFnSku() == null ? "" : summary.getFnSku());
        itemInventoryDo.setItemCondition(summary.getCondition());
        itemInventoryDo.setLastUpdatedTime(summary.getLastUpdatedTime().toString());
        itemInventoryDo.setAmazonQuantity(summary.getTotalQuantity());

        InventoryDetails details = summary.getInventoryDetails();
        itemInventoryDo.setFulfillableQuantity(details.getFulfillableQuantity());
        itemInventoryDo.setInboundWorkingQuantity(details.getInboundWorkingQuantity());
        itemInventoryDo.setInboundShippedQuantity(details.getInboundShippedQuantity());
        itemInventoryDo.setInboundReceivingQuantity(details.getInboundReceivingQuantity());
        itemInventoryDo.setReservedQuantity(JSONObject.toJSONString(details.getReservedQuantity()));
        itemInventoryDo.setResearchingQuantity(JSONObject.toJSONString(details.getResearchingQuantity()));
        itemInventoryDo.setUnfulfillableQuantity(JSONObject.toJSONString(details.getUnfulfillableQuantity()));

        itemInventoryDo.setUserMarketId(userMarketId);

        //设置初始值
        if (itemInventoryDo.getId() == null) {
            itemInventoryDo.setLocalQuantity(0);
        }
        itemInventoryDo.calculateTotalQuantity();
    }

    public static AmazonOrderDo convertToAmazonOrderDo(AmazonOrderDo orderDO, Order order) throws ParseException {
        orderDO.setAmazonOrderId(order.getAmazonOrderId());
        orderDO.setSellerOrderId(order.getSellerOrderId());
        orderDO.setPurchaseDate(TimeUtil.transform(order.getPurchaseDate()));
        orderDO.setLastUpdateDate(TimeUtil.transform(order.getLastUpdateDate()));
        orderDO.setOrderStatus(order.getOrderStatus().getValue());
        orderDO.setFulfillmentChannel(order.getFulfillmentChannel().getValue());
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
        orderDO.setPaymentMethod(order.getPaymentMethod().getValue());
        orderDO.setMarketplaceId(order.getMarketplaceId());
        if (order.getBuyerInfo() != null) {
            orderDO.setBuyerEmail(order.getBuyerInfo().getBuyerEmail());
            orderDO.setBuyerName(order.getBuyerInfo().getBuyerName());
        }
        orderDO.setShipmentServiceLevelCategory(order.getShipmentServiceLevelCategory());
        orderDO.setCbaDisplayableShippingLabel(order.getCbaDisplayableShippingLabel());
        orderDO.setOrderType(order.getOrderType().getValue());
        orderDO.setEarliestShipDate(order.getEarliestShipDate() != null ? TimeUtil.transformUTCToDate(order.getEarliestShipDate()) : null);
        orderDO.setLatestShipDate(order.getLatestShipDate() != null ? TimeUtil.transformUTCToDate(order.getLatestShipDate()) : null);
        orderDO.setEarliestDeliveryDate(order.getEarliestDeliveryDate() != null ? TimeUtil.transformUTCToDate(order.getEarliestDeliveryDate()) : null);
        orderDO.setLatestDeliveryDate(order.getLatestDeliveryDate() != null ? TimeUtil.transformUTCToDate(order.getLatestDeliveryDate()) : null);
        orderDO.setIsReplacementOrder(order.isIsReplacementOrder() ? 1 : 0);
        orderDO.setIsBusinessOrder(order.isIsBusinessOrder() ? 1 : 0);
        orderDO.setIsGlobalExpressEnabled(order.isIsGlobalExpressEnabled() ? 1 : 0);
        orderDO.setIsSoldByAB(order.isIsSoldByAB() ? 1 : 0);
        orderDO.setIsPremiumOrder(order.isIsPremiumOrder() ? 1 : 0);
        orderDO.setIsISPU(order.isIsISPU() ? 1 : 0);
        orderDO.setIsPrime(order.isIsPrime() ? 1 : 0);
        return orderDO;
    }

    public static AmazonOrderItemDo convertToOrderItemDO(AmazonOrderItemDo orderItemDO, OrderItem orderItem, String amazonOrderId) throws ParseException {
        orderItemDO.setAmazonOrderId(amazonOrderId);
        orderItemDO.setAsin(orderItem.getASIN());
        orderItemDO.setSku(orderItem.getSellerSKU());
        orderItemDO.setOrderItemId(orderItem.getOrderItemId());
        orderItemDO.setTitle(orderItem.getTitle());
        orderItemDO.setQuantityOrdered(orderItem.getQuantityOrdered());
        orderItemDO.setQuantityShipped(orderItem.getQuantityShipped());

        if (orderItem.getItemPrice() != null) {
            orderItemDO.setItemPriceAmount(Double.valueOf(orderItem.getItemPrice().getAmount()));
            orderItemDO.setItemPriceCurrencyCode(orderItem.getItemPrice().getCurrencyCode());
        }
        if (orderItem.getItemTax() != null) {
            orderItemDO.setItemTaxAmount(Double.valueOf(orderItem.getItemTax().getAmount()));
            orderItemDO.setItemTaxCurrencyCode(orderItem.getItemTax().getCurrencyCode());
        }

        if (orderItem.getShippingPrice() != null) {
            orderItemDO.setShippingPriceAmount(Double.valueOf(orderItem.getShippingPrice().getAmount()));
            orderItemDO.setShippingPriceCurrencyCode(orderItem.getShippingPrice().getCurrencyCode());
        }
        if (orderItem.getShippingTax() != null) {
            orderItemDO.setShippingTaxAmount(Double.valueOf(orderItem.getShippingTax().getAmount()));
            orderItemDO.setShippingTaxCurrencyCode(orderItem.getShippingTax().getCurrencyCode());
        }

        if (orderItem.getShippingDiscount() != null) {
            orderItemDO.setShippingDiscountAmount(Double.valueOf(orderItem.getShippingDiscount().getAmount()));
            orderItemDO.setShippingDiscountCurrencyCode(orderItem.getShippingDiscount().getCurrencyCode());
        }
        if (orderItem.getShippingDiscountTax() != null) {
            orderItemDO.setShippingDiscountTaxAmount(Double.valueOf(orderItem.getShippingDiscountTax().getAmount()));
            orderItemDO.setShippingDiscountTaxCurrencyCode(orderItem.getShippingDiscountTax().getCurrencyCode());
        }

//        if (orderItem.getGiftWrapPrice() != null) {
//            orderItemDO.setGiftWrapPriceAmount(orderItem.getGiftWrapPrice().getAmount());
//            orderItemDO.setGiftWrapPriceCurrencyCode(orderItem.getGiftWrapPrice().getCurrencyCode());
//        }
//        if (orderItem.getGiftWrapTax() != null) {
//            orderItemDO.setGiftWrapTaxAmount(orderItem.getGiftWrapTax().getAmount());
//            orderItemDO.setGiftWrapTaxCurrencyCode(orderItem.getGiftWrapTax().getCurrencyCode());
//        }

        if (orderItem.getPromotionDiscount() != null) {
            orderItemDO.setPromotionDiscountAmount(Double.valueOf(orderItem.getPromotionDiscount().getAmount()));
            orderItemDO.setPromotionDiscountCurrencyCode(orderItem.getPromotionDiscount().getCurrencyCode());
        }
        if (orderItem.getPromotionDiscountTax() != null) {
            orderItemDO.setPromotionDiscountTaxAmount(Double.valueOf(orderItem.getPromotionDiscountTax().getAmount()));
            orderItemDO.setPromotionDiscountTaxCurrencyCode(orderItem.getPromotionDiscountTax().getCurrencyCode());
        }

        orderItemDO.setPromotionIds(JSONObject.toJSONString(orderItem.getPromotionIds()));

        if (orderItem.getCoDFee() != null) {
            orderItemDO.setCodFeeAmount(Double.valueOf(orderItem.getCoDFee().getAmount()));
            orderItemDO.setCodFeeCurrencyCode(orderItem.getCoDFee().getCurrencyCode());
        }
        if (orderItem.getCoDFeeDiscount() != null) {
            orderItemDO.setCodFeeDiscountAmount(Double.valueOf(orderItem.getCoDFeeDiscount().getAmount()));
            orderItemDO.setCodFeeDiscountCurrencyCode(orderItem.getCoDFeeDiscount().getCurrencyCode());
        }

        orderItemDO.setConditionNote(orderItem.getConditionNote());
        orderItemDO.setConditionId(orderItem.getConditionId());
        orderItemDO.setConditionSubtypeId(orderItem.getConditionSubtypeId());

        orderItemDO.setScheduledDeliveryStartDate(orderItem.getScheduledDeliveryStartDate() != null ? TimeUtil.transform(orderItem.getScheduledDeliveryStartDate()) : null);
        orderItemDO.setScheduledDeliveryEndDate(orderItem.getScheduledDeliveryEndDate() != null ? TimeUtil.transform(orderItem.getScheduledDeliveryEndDate()) : null);

        orderItemDO.setTaxCollection(JSONObject.toJSONString(orderItem.getTaxCollection()));
        orderItemDO.setProductInfo(JSONObject.toJSONString(orderItem.getProductInfo()));
        if (orderItem.isIsGift() != null) {
            orderItemDO.setIsGift(orderItem.isIsGift() ? 1 : 0);
        } else {
            orderItemDO.setIsGift(0);
        }
        if (orderItem.isIsTransparency() != null) {
            orderItemDO.setIsTransparency(orderItem.isIsTransparency() ? 1 : 0);
        } else {
            orderItemDO.setIsTransparency(0);
        }
        return orderItemDO;
    }

    public static FbaInboundDo convertToShipmentInfoDO(FbaInboundDo fbaInboundDo, InboundShipmentInfo shipmentMember) {
        fbaInboundDo.setFcid(shipmentMember.getDestinationFulfillmentCenterId());
        fbaInboundDo.setLpType(shipmentMember.getLabelPrepType().getValue());
        fbaInboundDo.setShipAddress(JSONObject.toJSONString(shipmentMember.getShipFromAddress()));
        fbaInboundDo.setShipmentId(shipmentMember.getShipmentId());
        fbaInboundDo.setShipmentName(shipmentMember.getShipmentName());
        fbaInboundDo.setBoxContentsSource(shipmentMember.getBoxContentsSource() == null ? "" : shipmentMember.getBoxContentsSource().getValue());
        fbaInboundDo.setShipmentStatus(shipmentMember.getShipmentStatus().getValue());
        return fbaInboundDo;
    }

    public static FbaInboundItemDo convertToShipmentItemDO(FbaInboundItemDo fbaInboundItemDo, InboundShipmentItem member) {
        fbaInboundItemDo.setQuantityShipped(member.getQuantityShipped());
        fbaInboundItemDo.setShipmentId(member.getShipmentId());
        fbaInboundItemDo.setPrepDetailsList(JSONObject.toJSONString(member.getPrepDetailsList()));
        fbaInboundItemDo.setFulfillmentNetworkSKU(member.getFulfillmentNetworkSKU());
        fbaInboundItemDo.setSellerSKU(member.getSellerSKU());
        fbaInboundItemDo.setQuantityReceived(member.getQuantityReceived());
        fbaInboundItemDo.setQuantityInCase(member.getQuantityInCase());
        return fbaInboundItemDo;
    }
}
