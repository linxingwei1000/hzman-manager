package com.cn.hzm.core.sellerapi;

import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.amazon.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.amazon.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.cn.hzm.core.aws.Encrypt;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Maps;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import static com.amazon.SellingPartnerAPIAA.ScopeConstants.SCOPE_MIGRATION_API;
import static com.amazon.SellingPartnerAPIAA.ScopeConstants.SCOPE_NOTIFICATIONS_API;

/**
 * @author linxingwei
 * @date 8.4.22 3:54 下午
 */
public class test {

    //美西地址
    public static String US_URI = "https://sellingpartnerapi-na.amazon.com";

    //美国商城id
    public static String MARKET_PLACE_ID = "ATVPDKIKX0DER";


    public static void main(String[] args) {
        AWSAuthenticationCredentials awsAuthenticationCredentials = AWSAuthenticationCredentials.builder()
                .accessKeyId("AKIAZZXRVBF5KSAPKQ4D")
                .secretKey("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")
                .region("us-east-1")
                .build();

        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = AWSAuthenticationCredentialsProvider.builder()
                .roleArn("arn:aws:iam::673742915962:role/role_a")
                .roleSessionName(UUID.randomUUID().toString())
                .build();

        LWAAuthorizationCredentials lwaAuthorizationCredentials = LWAAuthorizationCredentials.builder()
                .clientId("amzn1.application-oa2-client.4d99cf8f9a474b058d92a1e2187908e5")
                .clientSecret("890d2554f9986039aedd15371eea6727f42de695d2b0cd0e3cb302c6765f6d0c")
                .refreshToken("Atzr|IwEBIOJOrNQg8CejX6CJcr2E3wMkNFLCnfs8SBDGDNgNLHRU3dzvE0zX_GBsB9K21L_XSpfnK5Zu3lLZzwVpnmT8YY1NRDyG-YiPp97JsVlZVyRzN10N_LnKid9sG028AvN2qdgt2PR_v_o570yJBKUAnN1HFs_6XXkCqeGkLkFpech7gYBKthAaXQqBcuHgbbue6pCUqb_0JxWvPcRa94s4t6k_mlblfsaY_6kBEEi1tQvslaMlDPciNxWOFfyui-wWoYFJ0U5NxflDe3M4MMBmzoFsNUSnBMB1aQSqBqQAbUBSpZ_WhpR0ujVG7YEFnRs7JwM")
                .withScopes(SCOPE_NOTIFICATIONS_API, SCOPE_MIGRATION_API)
                .endpoint("https://api.amazon.com/auth/o2/token")
                .build();

    }

//    public static void main(String[] args) throws Exception {
//        String url = "https://sellingpartnerapi-na.amazon.com/listings/2021-08-01/items/AK0HQWR8PUJRG/B07ZQHVGKX?marketplaceIds=ATVPDKIKX0DER";
//
//        Map<String, String> headerMap = Maps.newTreeMap(String::compareTo);
//        headerMap.put("host", "sellingpartnerapi-na.amazon.com");
//        headerMap.put("x-amz-access-token", "Atza|IwEBIA3ukZgzr2hPzmYvk7X8EWCIMV9MDCAcGah4wOw3e9Y0VeBA5kC1RzjY2iSd-vYYFpXtFTIpOd2yB1cVKTnKnlepzxMekvB0VlDttG4q_G1hE3zHuyYmIqLwZPwuaqDAqEfOkjLa3Nk0UQ1NDJi-b-A05H6mMI5jee-iwrwXjqEGbOBWVBVgh223OZpxX2YlEhYK6ZmifFDGKd1GUeoR3nHft69LFZcYTCkAY9kEAMRcuc1ODmUaXcH1f38zjNBUO7e3OG-y0YMMN37e-a-KJK53TCCWwwPikdMiqCXv7breRokcWgcsXBxFJ1LXojRzM60vKlLQv4cAcSU5_wLcXrN7");
//        String timestamp = TimeUtil.getSimpleUTC();
//        headerMap.put("x-amz-date", timestamp);
//
//        StringBuilder canonicalRequest = new StringBuilder();
//        canonicalRequest.append("GET").append("\n");
//        canonicalRequest.append("/listings/catalog/2020-12-01/items/B07ZQHVGKX").append("\n");
//        canonicalRequest.append("marketplaceIds=ATVPDKIKX0DER").append("\n");
//
//        headerMap.forEach((k, v) -> canonicalRequest.append(k.toLowerCase()).append(":").append(v.trim()).append("\n"));
//
//        //补一个\n?
//        canonicalRequest.append("\n");
//
//        canonicalRequest.append(String.join(";", headerMap.keySet())).append("\n");
//
//        //有效负载：这边有效负载为空字符串
//        canonicalRequest.append("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
//        System.out.println(canonicalRequest.toString());
//        String hashStr = Encrypt.getSHA256Str(canonicalRequest.toString());
//        System.out.println("hash:" + hashStr);
//        System.out.println("--------------------");
//
//
//        StringBuilder stringToSign = new StringBuilder();
//        stringToSign.append("AWS4-HMAC-SHA256").append("\n");
//        stringToSign.append(timestamp).append("\n");
//
//        String dateStr = timestamp.substring(0, timestamp.indexOf("T"));
//        String region = "us-east-1";
//        String service = "execute-api";
//        String versionAws = "aws4_request";
//        String area = dateStr + "/" + region + "/" + service + "/" + versionAws;
//        stringToSign.append(area).append("\n");
//        stringToSign.append(hashStr);
//        System.out.println("str to sign:\n" + stringToSign.toString());
//        System.out.println("--------------------");
//
//
//        String kSecret = "AWS4" + "PJ5FG/PZdkbKawhcsnh5WuPkdAtt1LTnWAAf59Tm";
//        byte[] kDate = Encrypt.hmacsha256(kSecret.getBytes(StandardCharsets.UTF_8), dateStr);
//        byte[] kRegion = Encrypt.hmacsha256(kDate, region);
//        byte[] kService = Encrypt.hmacsha256(kRegion, service);
//        byte[] kSigning = Encrypt.hmacsha256(kService, versionAws);
//        String sign = Encrypt.hmacsha256WithHex(kSigning, stringToSign.toString());
//        System.out.println("sign: " + sign);
//        System.out.println("--------------------");
//
//        StringBuilder authStr = new StringBuilder();
//        authStr.append("AWS4-HMAC-SHA256").append(" ")
//                .append("Credential=").append("AKIAZZXRVBF5MYAWQVPY").append("/").append(area).append(", ")
//                .append("SignedHeaders=").append(String.join(";", headerMap.keySet())).append(", ")
//                .append("Signature=").append(sign);
//        System.out.println("auth:" + authStr);
//
//
////        String canonicalRequestB = "GET" + "\n" +
////                "/" + "\n" +
////                "Action=ListUsers&Version=2010-05-08" + "\n" +
////                "content-type:application/x-www-form-urlencoded; charset=utf-8" + "\n" +
////                "host:iam.amazonaws.com" + "\n" +
////                "x-amz-date:20150830T123600Z" + "\n" + "\n" +
////                "content-type;host;x-amz-date" + "\n" +
////                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
////        String canonicalRequestSign = Encrypt.getSHA256Str(canonicalRequestB);
////        System.out.println(canonicalRequestSign);
////
////        StringBuilder stringToSignB = new StringBuilder();
////        stringToSignB.append("AWS4-HMAC-SHA256").append("\n");
////        stringToSignB.append("20150830T123600Z").append("\n");
////        stringToSignB.append("20150830/us-east-1/iam/aws4_request").append("\n");
////        stringToSignB.append(canonicalRequestSign);
////        System.out.println(stringToSignB);
////
////        String kSecretB = "AWS4" + "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
////        byte[] kDateB = Encrypt.hmacsha256(kSecretB.getBytes(StandardCharsets.UTF_8), "20150830");
////        byte[] kRegionB = Encrypt.hmacsha256(kDateB, "us-east-1");
////        byte[] kServiceB = Encrypt.hmacsha256(kRegionB, "iam");
////        byte[] kSigningB = Encrypt.hmacsha256(kServiceB, "aws4_request");
////        System.out.println(Encrypt.byte2Hex(kSigningB));
////        System.out.println(Encrypt.hmacsha256WithHex(kSigningB, stringToSignB.toString()));
//    }
}
