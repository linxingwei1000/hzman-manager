package com.cn.hzm.core.spa;

import com.alibaba.fastjson.JSONObject;
import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.amazon.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.repository.entity.AwsUserDo;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.spa.fbainbound.FbaInboundApi;
import com.cn.hzm.core.spa.fbainbound.model.GetShipmentItemsResponse;
import com.cn.hzm.core.spa.fbainbound.model.GetShipmentsResponse;
import com.cn.hzm.core.spa.fbainventory.FbaInventoryApi;
import com.cn.hzm.core.spa.fbainventory.model.GetInventorySummariesResponse;
import com.cn.hzm.core.spa.finance.FinanceApi;
import com.cn.hzm.core.spa.finance.model.ListFinancialEventsResponse;
import com.cn.hzm.core.spa.item.CatalogApi;
import com.cn.hzm.core.spa.item.model.Item;
import com.cn.hzm.core.spa.item.model.ItemSearchResults;
import com.cn.hzm.core.spa.order.OrdersV0Api;
import com.cn.hzm.core.spa.order.ShipmentApi;
import com.cn.hzm.core.spa.order.model.GetOrderItemsResponse;
import com.cn.hzm.core.spa.order.model.GetOrdersResponse;
import com.cn.hzm.core.spa.price.ProductPricingApi;
import com.cn.hzm.core.spa.price.model.GetPricingResponse;
import com.cn.hzm.core.spa.seller.SellersApi;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.threeten.bp.OffsetDateTime;

import java.util.List;
import java.util.UUID;

/**
 * @author linxingwei
 * @date 30.5.23 6:08 下午
 */
@Slf4j
public class SpaManager {

    private AwsUserDo awsUserDo;

    private AwsMarket awsMarket;

    private AwsUserMarketDo awsUserMarketDo;

    private String sign;

    /**
     * 卖家中心api
     */
    private SellersApi sellersApi;

    /**
     * 商品api
     */
    private CatalogApi catalogApi;

    /**
     * 仓储api
     */
    private FbaInventoryApi fbaInventoryApi;

    /**
     * 入口单api
     */
    private FbaInboundApi fbaInboundApi;

    /**
     * 价格api
     */
    private ProductPricingApi productPricingApi;

    /**
     * 订单api
     */
    private OrdersV0Api ordersV0Api;


    /**
     * 货运单api
     */
    private ShipmentApi shipmentApi;

    /**
     * 财务api
     */
    private FinanceApi financeApi;

    private static final String API_END_POINT = "https://api.amazon.com/auth/o2/token";

    private static final List<String> ITEM_DATA = Lists.newArrayList();

    static {
        ITEM_DATA.add("summaries");
        ITEM_DATA.add("attributes");
        ITEM_DATA.add("dimensions");
        ITEM_DATA.add("identifiers");
        ITEM_DATA.add("images");
        ITEM_DATA.add("productTypes");
        ITEM_DATA.add("relationships");
        ITEM_DATA.add("salesRanks");
    }

    /**
     * 获取市场id
     *
     * @return
     */
    public String getMarketId() {
        return awsMarket.getId();
    }

    public Integer getAwsUserId() {
        return awsUserDo.getId();
    }

    public Integer getAwsUserMarketId() {
        return awsUserMarketDo.getId();
    }

    /**
     * 根据asin获取亚马逊商品
     *
     * @param asin
     * @return
     * @throws ApiException
     */
    public Item getItemByAsin(String asin){
        try {
            return catalogApi.getCatalogItem(asin, Lists.newArrayList(awsMarket.getId()), ITEM_DATA, null);
        } catch (ApiException e) {
            log.error("获取商品失败：", e);
        }
        return null;
    }

    /**
     * 根据sku获取亚马逊商品
     *
     * @param sku
     * @return
     * @throws ApiException
     */
    public Item getItemBySku(String sku) throws ApiException {
        //todo sellerId 需要替换
        ItemSearchResults results = catalogApi.searchCatalogItems(Lists.newArrayList(awsMarket.getId()), Lists.newArrayList(sku), "SKU", ITEM_DATA,
                null, "AK0HQWR8PUJRG", null, null, null,
                null, null, null);
        if (!CollectionUtils.isEmpty(results.getItems())) {
            return results.getItems().get(0);
        }
        return null;
    }

    /**
     * 根据asin获取价格
     *
     * @param asin
     * @return
     * @throws ApiException
     */
    public GetPricingResponse getPriceByAsin(String asin) throws ApiException {
        return productPricingApi.getPricing(awsMarket.getId(), "Asin", null, Lists.newArrayList(asin)
                , null, null);
    }

    /**
     * 根据sku获取价格
     *
     * @param sku
     * @return
     * @throws ApiException
     */
    public GetPricingResponse getPriceBySku(String sku) {
        try {
            return productPricingApi.getPricing(awsMarket.getId(), "Sku", null, Lists.newArrayList(sku)
                    , null, null);
        } catch (ApiException e) {
            log.error("获取商品价格失败：", e);
        }
        return null;
    }


    public GetInventorySummariesResponse getInventoryInfoBySku(String sku) {
        try {
            return fbaInventoryApi.getInventorySummaries("Marketplace",
                    awsMarket.getId(), Lists.newArrayList(awsMarket.getId()), true, null,
                    Lists.newArrayList(sku), null);
        } catch (ApiException e) {
            log.error("[{}]获取库存信息失败：", sku, e);
        }
        return null;
    }


    /**
     * 根据amazonOrderId获取订单
     *
     * @param orderIds
     * @return
     * @throws ApiException
     */
    public GetOrdersResponse orderListByOrderIds(List<String> orderIds) throws ApiException {
        return ordersV0Api.getOrders(Lists.newArrayList(awsMarket.getId()), null, null
                , null, null, null, null, null, null,
                null, null, null, null, null, orderIds,
                null, null, null);
    }


    /**
     * 按时间范围获取订单
     *
     * @param beginTime
     * @param endTime
     * @return
     * @throws ApiException
     */
    public GetOrdersResponse orderList(String beginTime, String endTime) throws ApiException {
        return ordersV0Api.getOrders(Lists.newArrayList(awsMarket.getId()), beginTime, endTime
                , null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null);
    }

    /**
     * 按nextToken获取订单
     *
     * @param nextToken
     * @return
     * @throws ApiException
     */
    public GetOrdersResponse orderListByNextToken(String nextToken) throws ApiException {
        return ordersV0Api.getOrders(Lists.newArrayList(awsMarket.getId()), null, null
                , null, null, null, null, null, null,
                null, null, null, null, nextToken, null,
                null, null, null);
    }

    public GetOrderItemsResponse orderItems(String orderId, String nextToken) throws ApiException {
        return ordersV0Api.getOrderItems(orderId, nextToken);
    }

    public GetShipmentsResponse getShipmentsByDateRange(List<String> status, OffsetDateTime lastUpdatedAfter, OffsetDateTime lastUpdatedBefore) {
        //queryType：SHIPMENT, DATE_RANGE, NEXT_TOKEN
        try {
            return fbaInboundApi.getShipments("DATE_RANGE",
                    awsMarket.getId(), status, null, lastUpdatedAfter, lastUpdatedBefore, null);
        } catch (ApiException e) {
            log.error("获取入库单失败：", e);
        }
        return null;
    }

    public GetShipmentsResponse getShipmentsByShipmentIds(List<String> shipmentIds) {
        //queryType：SHIPMENT, DATE_RANGE, NEXT_TOKEN
        try {
            return fbaInboundApi.getShipments("SHIPMENT",
                    awsMarket.getId(), null, shipmentIds, null, null, null);
        } catch (ApiException e) {
            log.error("获取入库单失败：", e);
        }
        return null;
    }

    public GetShipmentsResponse getShipmentsByNextToken(String nextToken) {
        //queryType：SHIPMENT, DATE_RANGE, NEXT_TOKEN
        try {
            return fbaInboundApi.getShipments("NEXT_TOKEN",
                    awsMarket.getId(), null, null, null, null, nextToken);
        } catch (ApiException e) {
            log.error("获取入库单失败：", e);
        }
        return null;
    }

    public GetShipmentItemsResponse getShipmentItemsByShipmentId(String shipmentId) throws ApiException {
        return fbaInboundApi.getShipmentItemsByShipmentId(shipmentId, awsMarket.getId());
    }

    public ListFinancialEventsResponse getFinanceByAwsOrderId(String orderId){
        try {
            return financeApi.listFinancialEventsByOrderId(orderId, null, null);
        } catch (ApiException e) {
            log.error("获取财务信息失败：", e);
        }
        return null;
    }

    /**
     * 初始化spa 调用
     *
     * @param awsUserDo
     * @param awsMarket
     * @param awsUserMarketDo
     */
    public SpaManager(AwsUserDo awsUserDo, AwsMarket awsMarket, AwsUserMarketDo awsUserMarketDo) {
        AWSAuthenticationCredentials awsAuthenticationCredentials = AWSAuthenticationCredentials.builder()
                .accessKeyId(awsUserDo.getAccessKeyId())
                .secretKey(awsUserDo.getSecretKey())
                .region(awsMarket.getRegion())
                .build();

        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = AWSAuthenticationCredentialsProvider.builder()
                .roleArn(awsUserDo.getRoleArn())
                .roleSessionName(UUID.randomUUID().toString())
                .build();

        LWAAuthorizationCredentials lwaAuthorizationCredentials = LWAAuthorizationCredentials.builder()
                .clientId(awsUserDo.getClientId())
                .clientSecret(awsUserDo.getClientSecret())  //新密钥
                .refreshToken(awsUserDo.getRefreshToken())
                //.withScopes(SCOPE_NOTIFICATIONS_API, SCOPE_MIGRATION_API)
                .endpoint(API_END_POINT)
                .build();

        this.awsUserDo = awsUserDo;
        this.awsMarket = awsMarket;
        this.awsUserMarketDo = awsUserMarketDo;
        this.sign = awsUserDo.getRemark();

        this.sellersApi = new SellersApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] sellersApi", awsUserDo.getRemark());

        this.ordersV0Api = new OrdersV0Api.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] ordersV0Api", awsUserDo.getRemark());

        this.shipmentApi = new ShipmentApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] shipmentApi", awsUserDo.getRemark());

        this.catalogApi = new CatalogApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] catalogApi", awsUserDo.getRemark());

        this.fbaInventoryApi = new FbaInventoryApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] fbaInventoryApi", awsUserDo.getRemark());

        this.fbaInboundApi = new FbaInboundApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] fbaInboundApi", awsUserDo.getRemark());

        this.financeApi = new FinanceApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        log.info("初始化[{}] financeApi", awsUserDo.getRemark());
    }

    public static void main(String[] args) throws ApiException {
        AwsUserDo awsUserDo = new AwsUserDo();
        awsUserDo.setAccessKeyId("AKIAZZXRVBF5KSAPKQ4D");
        awsUserDo.setSecretKey("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");
        awsUserDo.setRoleArn("arn:aws:iam::673742915962:role/role_a");
        awsUserDo.setClientId("amzn1.application-oa2-client.4d99cf8f9a474b058d92a1e2187908e5");
        awsUserDo.setClientSecret("amzn1.oa2-cs.v1.40e67554e0b100112b3b74f98d9bea5e316df7c135cdab4270bcab62588e1f9a");
        awsUserDo.setRefreshToken("Atzr|IwEBIOJOrNQg8CejX6CJcr2E3wMkNFLCnfs8SBDGDNgNLHRU3dzvE0zX_GBsB9K21L_XSpfnK5Zu3lLZzwVpnmT8YY1NRDyG-YiPp97JsVlZVyRzN10N_LnKid9sG028AvN2qdgt2PR_v_o570yJBKUAnN1HFs_6XXkCqeGkLkFpech7gYBKthAaXQqBcuHgbbue6pCUqb_0JxWvPcRa94s4t6k_mlblfsaY_6kBEEi1tQvslaMlDPciNxWOFfyui-wWoYFJ0U5NxflDe3M4MMBmzoFsNUSnBMB1aQSqBqQAbUBSpZ_WhpR0ujVG7YEFnRs7JwM");

        AwsMarket awsMarket = AwsMarket.America;

        SpaManager spaManager = new SpaManager(awsUserDo, awsMarket, null);

        Item item = spaManager.getItemByAsin("B0BNPF69NN");

        System.out.println(JSONObject.toJSONString(item));
    }
}
