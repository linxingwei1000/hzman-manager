package com.cn.hzm.core.spa;

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
import com.cn.hzm.core.spa.listings.ListingsApi;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
     * 列表api
     */
    private ListingsApi listingsApi;


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
    private Semaphore productPricingSemaphore;

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
    public Item getItemByAsin(String asin) {
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
        ItemSearchResults results = catalogApi.searchCatalogItems(Lists.newArrayList(awsMarket.getId()), Lists.newArrayList(sku), "SKU", ITEM_DATA,
                null, awsUserDo.getSellerId(), null, null, null,
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

    public com.cn.hzm.core.spa.listings.model.Item getListingsItem(String sku) throws ApiException {
        //List<String> includeDatas = Lists.newArrayList("summaries","attributes","issues","offers","fulfillmentAvailability","procurement");
        List<String> includeDatas = Lists.newArrayList("summaries");
        com.cn.hzm.core.spa.listings.model.Item item = listingsApi.getListingsItem(awsUserDo.getSellerId(), sku, Lists.newArrayList(awsMarket.getId()), null, includeDatas);
        return item;
    }

    /**
     * 根据sku获取价格
     *
     * @param sku
     * @return
     * @throws ApiException
     */
    public GetPricingResponse getPriceBySku(String sku) {
        int retryTime = 3;
        for (int i = 0; i < retryTime; i++) {
            try {
                productPricingSemaphore.acquire();
                return productPricingApi.getPricing(awsMarket.getId(), "Sku", null, Lists.newArrayList(sku)
                        , null, null);
            } catch (ApiException ae) {
                if (ae.getCode() == 429) {
                    log.info("{} 获取价格请求超限，重试获取， {}", sku, i);
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException ignored) { }
                }else{
                    log.error("{} 获取商品价格失败：", sku, ae);
                    break;
                }
            } catch (Exception e) {
                log.error("{} 获取商品价格失败：", sku, e);
                break;
            }
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

    public ListFinancialEventsResponse getFinanceByAwsOrderId(String orderId) {
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
                .refreshToken(awsUserMarketDo.getRefreshToken())
                //.withScopes(SCOPE_NOTIFICATIONS_API, SCOPE_MIGRATION_API)
                .endpoint(API_END_POINT)
                .build();

        this.awsUserDo = awsUserDo;
        this.awsMarket = awsMarket;
        this.awsUserMarketDo = awsUserMarketDo;
        this.sign = awsUserDo.getRemark();

        StringBuilder sb = new StringBuilder("[").append(awsUserDo.getId())
                .append("-").append(awsUserDo.getRemark())
                .append("-").append(awsMarket.getId()).append("]")
                .append("spaManager初始化：");
        this.sellersApi = new SellersApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("sellersApi").append(",");

        this.ordersV0Api = new OrdersV0Api.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("ordersV0Api").append(",");

        this.shipmentApi = new ShipmentApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("shipmentApi").append(",");

        this.catalogApi = new CatalogApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("catalogApi").append(",");

        this.listingsApi = new ListingsApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("listingsApi").append(",");

        this.productPricingApi = new ProductPricingApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("productPricingApi").append(",");
        productPricingSemaphore = new Semaphore(2);
        ScheduledThreadPoolExecutor getOrderScheduledTask = new ScheduledThreadPoolExecutor(1);
        getOrderScheduledTask.scheduleAtFixedRate(() -> {
            if (productPricingSemaphore.availablePermits() < 2) {
                productPricingSemaphore.release(1);
            }
        }, 5, 4, TimeUnit.SECONDS);

        this.fbaInventoryApi = new FbaInventoryApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("fbaInventoryApi").append(",");

        this.fbaInboundApi = new FbaInboundApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("fbaInboundApi").append(",");

        this.financeApi = new FinanceApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .endpoint(awsMarket.getEndpoint())
                .build();
        sb.append("financeApi").append(",");
        log.info("{}", sb);
    }

    public static void main(String[] args) throws ApiException {
        AwsUserDo awsUserDo = new AwsUserDo();
        awsUserDo.setSellerId("AK0HQWR8PUJRG");
        awsUserDo.setAccessKeyId("AKIAZZXRVBF5KSAPKQ4D");
        awsUserDo.setSecretKey("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");
        awsUserDo.setRoleArn("arn:aws:iam::673742915962:role/role_a");
        awsUserDo.setClientId("amzn1.application-oa2-client.0e3e98270af54054ba3148898578cead");
        awsUserDo.setClientSecret("amzn1.oa2-cs.v1.0e859bd13b89725dfd8809cfa98e7b9977c5bc95ad7606e65799c5bf1d73d995");
        //awsUserDo.setRefreshToken("Atzr|IwEBIL-T9IW6pj2GeRkkuYAdTnc5g3qTJA2xHXe-9B4hnBqz0870rvvVLDSTXdyi68G7ApiIBn8tpiSywsufuNO-QIoSTDKdM2_ytv5hUI2Z33X0kjIxaGGjRz3WSmbT7m6FPkzT1M3YLF1A5qcPgnnGJdko5D7HVebRg8wCYVaJ4d4KdXDw-zTG22fbO_lc8bNjTLwH_0RZD7Ru_VW2lTi7vckzU7VKn1fSdwQeltU6IVkkTdZrTB-UHiPxG1iHh9fVEaWXN7LFTo_CxNTjErIydxtlCa4SIEUKhKaCekr8amFBD64xt58L8KZKR2MlfU4v8u8");

        AwsUserMarketDo awsUserMarketDo = new AwsUserMarketDo();
        awsUserMarketDo.setRefreshToken("Atzr|IwEBIKKCwMBuzVX1JdlQf57N9gnZES2E2EijZL7b4FY-JQaBMPbzCTz8sZJHeat7Zl5OstRiCIDTcTw0uia6ENMm4bW4KjHqn3KKLkJVttbqCvFHm7siOKj6WETYx4B739cZD16Rm8nTXXoqlFi2yZxE7osYAPIMoSzuWFrDxIA9BIkyoZOteEIYrcyqzifvHz5zUtpOeU5xFwm_8fyOy_2qze8AyzJM7z1VwenWFaga6fIqPKNMWPWyS34XDAr5VDJ4pjE7rn0zsqn2DxDMpou3YPsoknoFLWqiahmip0AbEgiJpsD7ij2OAQHkvEZxG3g4mig");



        AwsMarket awsMarket = AwsMarket.Australia;

        SpaManager spaManager = new SpaManager(awsUserDo, awsMarket, awsUserMarketDo);

        Item r = spaManager.getItemBySku("S7828E-AU");
        //GetOrdersResponse r = spaManager.orderListByOrderIds(Lists.newArrayList("114-2809190-2935453"));
        //GetInventorySummariesResponse r = spaManager.getInventoryInfoBySku("SET23-0719-01B");
        //GetShipmentsResponse r = spaManager.getShipmentsByShipmentIds(Lists.newArrayList("FBA15DJC28G4"));
        System.out.println(r);
    }
}
