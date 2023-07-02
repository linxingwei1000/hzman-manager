package com.cn.hzm.core.spa;

import com.alibaba.fastjson.JSONObject;
import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.amazon.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.cn.hzm.core.spa.order.OrdersV0Api;
import com.cn.hzm.core.spa.order.model.GetOrderResponse;
import com.cn.hzm.core.spa.order.model.GetOrdersResponse;
import com.cn.hzm.core.spa.seller.SellersApi;
import com.cn.hzm.core.spa.seller.model.GetMarketplaceParticipationsResponse;
import com.google.common.collect.Lists;

import java.util.UUID;

/**
 * @author linxingwei
 * @date 22.2.23 4:39 下午
 */
public class test {

    public static void main(String[] args) throws ApiException {
        String endpoint = "https://sellingpartnerapi-na.amazon.com";

//        //卖家中心测试
//        SellersApi sellersApi = getSellersApi(endpoint);
//        GetMarketplaceParticipationsResponse resp = sellersApi.getMarketplaceParticipations();
//        ApiClient apiClient = sellersApi.getApiClient();


        //订单测试
        OrdersV0Api ordersV0Api = getOrdersV0Api(endpoint);
//        GetOrdersResponse response = ordersV0Api.getOrders(Lists.newArrayList("ATVPDKIKX0DER"), "2022-01-01T00:00:00Z", "2022-01-02T00:30:00Z"
//                , null, null, null, null, null, null,
//                null,null,null,null,null,null,
//                null,null, null);
////        String orderId = "113-3988923-1347419";
////        GetOrderResponse response = ordersV0Api.getOrder(orderId);
//        System.out.println(JSONObject.toJSONString(response));


        GetOrdersResponse response = ordersV0Api.getOrders(Lists.newArrayList("ATVPDKIKX0DER"), null, null
                , null, null, null, null, null, null,
                null,null,null,null,"B93gPtITY6qaJqJYLDm0ZAmQazDrhw3CORIB1MaJBDRUojdU4H46tsNI3HOI22PIxqXyQLkGMBs8VhF73Xgy+4qp7ZWwsKJzTXZhMhdef824qqfm+McWC/mDOU6fp6slInTAy+XKVmRZBY+oaVuycwQFure81U/C6cSoRowVGXaYeATp5zAOh7+CEIY3Nqd2f2GLmUGyr9UGnxD0RJmrryegoU0IPZxXEnlZHq31Y97LiJrto3jrN7SisnPCoFFxMBO/reDY2s8hGWNNu3VwFU0hrIUQcumU7YYXwRo+g8JiIXlRXylzxduZ3sLNMwtcrtWLrY5NAPybustTHy4bwEUt1tuCJsA358cNU4vCOymDkC0y9MthgqMRAFvkoLFpR1aswj95L0+BA1KyEG2j/LqeyFb5ax06drbmmw7x71Zj2KJJsSfIkA==",null,
                null,null, null);
        System.out.println(JSONObject.toJSONString(response));

    }

    private static SellersApi getSellersApi(String endPoint){
        SellersApi sellersApi = new SellersApi.Builder()
                .awsAuthenticationCredentials(getAWSAuthenticationCredentials())
                .lwaAuthorizationCredentials(getLWAAuthorizationCredentials())
                .awsAuthenticationCredentialsProvider(getAWSAuthenticationCredentialsProvider())
                .endpoint(endPoint)
                .build();
        return sellersApi;
    }

    private static OrdersV0Api getOrdersV0Api(String endPoint){
        OrdersV0Api ordersV0Api = new OrdersV0Api.Builder()
                .awsAuthenticationCredentials(getAWSAuthenticationCredentials())
                .lwaAuthorizationCredentials(getLWAAuthorizationCredentials())
                .awsAuthenticationCredentialsProvider(getAWSAuthenticationCredentialsProvider())
                .endpoint(endPoint)
                .build();
        return ordersV0Api;
    }

    private static AWSAuthenticationCredentials getAWSAuthenticationCredentials(){
        AWSAuthenticationCredentials awsAuthenticationCredentials = AWSAuthenticationCredentials.builder()
                .accessKeyId("AKIAZZXRVBF5KSAPKQ4D")
                .secretKey("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")
                .region("us-east-1")
                .build();
        return awsAuthenticationCredentials;
    }

    private static AWSAuthenticationCredentialsProvider getAWSAuthenticationCredentialsProvider(){
        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = AWSAuthenticationCredentialsProvider.builder()
                .roleArn("arn:aws:iam::673742915962:role/role_a")
                .roleSessionName(UUID.randomUUID().toString())
                .build();
        return awsAuthenticationCredentialsProvider;
    }

    private static LWAAuthorizationCredentials getLWAAuthorizationCredentials(){
        LWAAuthorizationCredentials lwaAuthorizationCredentials = LWAAuthorizationCredentials.builder()
                .clientId("amzn1.application-oa2-client.4d99cf8f9a474b058d92a1e2187908e5")
                .clientSecret("amzn1.oa2-cs.v1.40e67554e0b100112b3b74f98d9bea5e316df7c135cdab4270bcab62588e1f9a")  //新密钥
                .refreshToken("Atzr|IwEBIOJOrNQg8CejX6CJcr2E3wMkNFLCnfs8SBDGDNgNLHRU3dzvE0zX_GBsB9K21L_XSpfnK5Zu3lLZzwVpnmT8YY1NRDyG-YiPp97JsVlZVyRzN10N_LnKid9sG028AvN2qdgt2PR_v_o570yJBKUAnN1HFs_6XXkCqeGkLkFpech7gYBKthAaXQqBcuHgbbue6pCUqb_0JxWvPcRa94s4t6k_mlblfsaY_6kBEEi1tQvslaMlDPciNxWOFfyui-wWoYFJ0U5NxflDe3M4MMBmzoFsNUSnBMB1aQSqBqQAbUBSpZ_WhpR0ujVG7YEFnRs7JwM")
                //.withScopes(SCOPE_NOTIFICATIONS_API, SCOPE_MIGRATION_API)
                .endpoint("https://api.amazon.com/auth/o2/token")
                .build();
        return lwaAuthorizationCredentials;
    }
}
