package com.cn.hzm.core.enums;

import com.cn.hzm.core.constant.ContextConst;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 13.4.23 6:11 下午
 */
@Getter
@AllArgsConstructor
public enum AwsMarket {

    //北美区域
    Canada("A2EUQ1WTGCTBG2", "CA", "us-east-1", "https://sellingpartnerapi-na.amazon.com","加拿大", ""),
    America("ATVPDKIKX0DER", "US", "us-east-1", "https://sellingpartnerapi-na.amazon.com","美国", ""),
    Mexico("A1AM78C64UM0Y8", "MX", "us-east-1", "https://sellingpartnerapi-na.amazon.com","墨西哥", ""),
    Brazil("A2Q3Y263D00KWC", "BR", "us-east-1", "https://sellingpartnerapi-na.amazon.com","巴西", ""),

    //欧洲区域
    Spain("A1RKKUPIHCS9HS", "ES", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","西班牙", ".es"),
    England("A1F83G8C2ARO7P", "UK", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","英国", ".uk"),
    France("A13V1IB3VIYZZH", "FR", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","法国", ".fr"),
    Belgium("AMEN7PMS3EDWL", "BE", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","比利时", ".be"),
    Netherlands("A1805IZSGTT6HS", "NL", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","荷兰", ".nl"),
    Germany("A1PA6795UKMFR9", "DE", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","德国", ".de"),
    Italy("APJ6JRA9NG5V4", "IT", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","意大利", ".it"),
    Sweden("A2NODRKZP88ZB9", "SE", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","瑞典", ".se"),
    Poland("A1C3SOZRARQ6R3", "PL", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","波兰", ".pl"),
    Egypt("ARBP9OOSHTCHU", "EG", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","埃及", ".eg"),
    Turkey("A33AVAJ2PDY3EV", "TR", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","土耳其", ".tr"),
    SaudiArabia("A17E79C6D8DWNP", "SA", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","沙特阿拉伯", ".sa"),
    UnitedArabEmirates("A2VIGQ35RCS4UG", "AE", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","阿拉伯联合酋长国", ".ae"),
    India("A21TJRUUN4KGV", "IN", "eu-west-1", "https://sellingpartnerapi-eu.amazon.com","印度", ".in"),

    //东南亚区域
    Singapore("A19VAU5U5O7RUS", "SG", "us-west-2", "https://sellingpartnerapi-fe.amazon.com","新加坡", ".sg"),
    Australia("A39IBJ37TRP1C6", "AU", "us-west-2", "https://sellingpartnerapi-fe.amazon.com","澳大利亚", ".au"),
    Japan("A1VC38T7YXB528", "JP", "us-west-2", "https://sellingpartnerapi-fe.amazon.com","日本", ".jp"),

    ;

    private String id;

    private String countryCode;

    private String region;

    private String endpoint;

    private String desc;

    private String ares;


    public static AwsMarket getByMarketId(String marketId){
        for (AwsMarket awsMarket : values()) {
            if(awsMarket.getId().equals(marketId)){
                return awsMarket;
            }
        }
        return America;
    }

    public static List<Map<String, Object>> jsonEnum(){
        List<Map<String, Object>> enumList = Lists.newArrayList();
        for (AwsMarket awsMarket : values()) {
            Map<String, Object> enumMap = Maps.newHashMap();
            enumMap.put("id", awsMarket.getId());
            enumMap.put("desc", ContextConst.REGION_MAP.get(awsMarket.getRegion()) + "|" + awsMarket.getDesc());
            enumList.add(enumMap);
        }
        return enumList;
    }

}
