package com.cn.hzm.core.sellerapi;

import com.cn.hzm.core.aws.Encrypt;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Maps;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;

/**
 * @author linxingwei
 * @date 8.4.22 3:54 下午
 */
public class test {

    public static void main(String[] args) throws Exception {
        String url = "https://sellingpartnerapi-na.amazon.com/listings/2021-08-01/items/AK0HQWR8PUJRG/B07ZQHVGKX?marketplaceIds=ATVPDKIKX0DER";

        Map<String, String> headerMap = Maps.newTreeMap(String::compareTo);
        headerMap.put("host", "sellingpartnerapi-na.amazon.com");
        headerMap.put("x-amz-access-token", "Atza|IwEBIA3ukZgzr2hPzmYvk7X8EWCIMV9MDCAcGah4wOw3e9Y0VeBA5kC1RzjY2iSd-vYYFpXtFTIpOd2yB1cVKTnKnlepzxMekvB0VlDttG4q_G1hE3zHuyYmIqLwZPwuaqDAqEfOkjLa3Nk0UQ1NDJi-b-A05H6mMI5jee-iwrwXjqEGbOBWVBVgh223OZpxX2YlEhYK6ZmifFDGKd1GUeoR3nHft69LFZcYTCkAY9kEAMRcuc1ODmUaXcH1f38zjNBUO7e3OG-y0YMMN37e-a-KJK53TCCWwwPikdMiqCXv7breRokcWgcsXBxFJ1LXojRzM60vKlLQv4cAcSU5_wLcXrN7");
        String timestamp = TimeUtil.getSimpleUTC();
        headerMap.put("x-amz-date", timestamp);

        StringBuilder canonicalRequest = new StringBuilder();
        canonicalRequest.append("GET").append("\n");
        canonicalRequest.append("/listings/catalog/2020-12-01/items/B07ZQHVGKX").append("\n");
        canonicalRequest.append("marketplaceIds=ATVPDKIKX0DER").append("\n");

        headerMap.forEach((k, v) -> canonicalRequest.append(k.toLowerCase()).append(":").append(v.trim()).append("\n"));

        //补一个\n?
        canonicalRequest.append("\n");

        canonicalRequest.append(String.join(";", headerMap.keySet())).append("\n");

        //有效负载：这边有效负载为空字符串
        canonicalRequest.append("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        System.out.println(canonicalRequest.toString());
        String hashStr = Encrypt.getSHA256Str(canonicalRequest.toString());
        System.out.println("hash:" + hashStr);
        System.out.println("--------------------");


        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("AWS4-HMAC-SHA256").append("\n");
        stringToSign.append(timestamp).append("\n");

        String dateStr = timestamp.substring(0, timestamp.indexOf("T"));
        String region = "us-east-1";
        String service = "execute-api";
        String versionAws = "aws4_request";
        String area = dateStr + "/" + region + "/" + service + "/" + versionAws;
        stringToSign.append(area).append("\n");
        stringToSign.append(hashStr);
        System.out.println("str to sign:\n" + stringToSign.toString());
        System.out.println("--------------------");


        String kSecret = "AWS4" + "PJ5FG/PZdkbKawhcsnh5WuPkdAtt1LTnWAAf59Tm";
        byte[] kDate = Encrypt.hmacsha256(kSecret.getBytes(StandardCharsets.UTF_8), dateStr);
        byte[] kRegion = Encrypt.hmacsha256(kDate, region);
        byte[] kService = Encrypt.hmacsha256(kRegion, service);
        byte[] kSigning = Encrypt.hmacsha256(kService, versionAws);
        String sign = Encrypt.hmacsha256WithHex(kSigning, stringToSign.toString());
        System.out.println("sign: " + sign);
        System.out.println("--------------------");

        StringBuilder authStr = new StringBuilder();
        authStr.append("AWS4-HMAC-SHA256").append(" ")
                .append("Credential=").append("AKIAZZXRVBF5MYAWQVPY").append("/").append(area).append(", ")
                .append("SignedHeaders=").append(String.join(";", headerMap.keySet())).append(", ")
                .append("Signature=").append(sign);
        System.out.println("auth:" + authStr);


//        String canonicalRequestB = "GET" + "\n" +
//                "/" + "\n" +
//                "Action=ListUsers&Version=2010-05-08" + "\n" +
//                "content-type:application/x-www-form-urlencoded; charset=utf-8" + "\n" +
//                "host:iam.amazonaws.com" + "\n" +
//                "x-amz-date:20150830T123600Z" + "\n" + "\n" +
//                "content-type;host;x-amz-date" + "\n" +
//                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
//        String canonicalRequestSign = Encrypt.getSHA256Str(canonicalRequestB);
//        System.out.println(canonicalRequestSign);
//
//        StringBuilder stringToSignB = new StringBuilder();
//        stringToSignB.append("AWS4-HMAC-SHA256").append("\n");
//        stringToSignB.append("20150830T123600Z").append("\n");
//        stringToSignB.append("20150830/us-east-1/iam/aws4_request").append("\n");
//        stringToSignB.append(canonicalRequestSign);
//        System.out.println(stringToSignB);
//
//        String kSecretB = "AWS4" + "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
//        byte[] kDateB = Encrypt.hmacsha256(kSecretB.getBytes(StandardCharsets.UTF_8), "20150830");
//        byte[] kRegionB = Encrypt.hmacsha256(kDateB, "us-east-1");
//        byte[] kServiceB = Encrypt.hmacsha256(kRegionB, "iam");
//        byte[] kSigningB = Encrypt.hmacsha256(kServiceB, "aws4_request");
//        System.out.println(Encrypt.byte2Hex(kSigningB));
//        System.out.println(Encrypt.hmacsha256WithHex(kSigningB, stringToSignB.toString()));
    }
}
