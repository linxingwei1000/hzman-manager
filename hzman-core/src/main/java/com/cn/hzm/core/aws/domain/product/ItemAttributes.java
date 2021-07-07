package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:55 下午
 */
@Data
@XStreamAlias(value="ns2:ItemAttributes")
public class ItemAttributes {

    @XStreamAlias(value="ns2:Binding")
    private String binding;

    @XStreamAlias(value="ns2:Brand")
    private String brand;

    @XStreamAlias(value="ns2:ChainType")
    private String chainType;

    @XStreamAlias(value="ns2:ClaspType")
    private String claspType;

    @XStreamAlias(value="ns2:Color")
    private String color;

    @XStreamAlias(value="ns2:Department")
    private String department;

    @XStreamImplicit(itemFieldName="ns2:GemType")
    private List<String> gemType;

    @XStreamAlias(value="ns2:Label")
    private String label;

    @XStreamAlias(value="ns2:Manufacturer")
    private String manufacturer;

    @XStreamImplicit(itemFieldName="ns2:MaterialType")
    private List<String> materialType;

    @XStreamAlias(value="ns2:MetalStamp")
    private String metalStamp;

    @XStreamAlias(value="ns2:MetalType")
    private String metalType;

    @XStreamAlias(value="ns2:Model")
    private String model;

    @XStreamAlias(value="ns2:PackageDimensions")
    private PackageDimensions packageDimensions;

    @XStreamAlias(value="ns2:PartNumber")
    private String partNumber;

    @XStreamAlias(value="ns2:ProductGroup")
    private String productGroup;

    @XStreamAlias(value="ns2:ProductTypeName")
    private String productTypeName;

    @XStreamAlias(value="ns2:Publisher")
    private String publisher;

    @XStreamAlias(value="ns2:SmallImage")
    private SmallImage smallImage;

    @XStreamAlias(value="ns2:Studio")
    private String studio;

    @XStreamAlias(value="ns2:Title")
    private String title;

    @XStreamAlias(value="ns2:ListPrice")
    private String listPrice;

    @XStreamAlias(value="ns2:ItemDimensions")
    private String itemDimensions;

    @XStreamAlias(value="ns2:IsAutographed")
    private String isAutographed;

    @XStreamAlias(value="ns2:IsMemorabilia")
    private String isMemorabilia;

    @XStreamAlias(value="ns2:PackageQuantity")
    private String packageQuantity;

    @XStreamAlias(value="ns2:RingSize")
    private String ringSize;

    @XStreamAlias(value="ns2:TotalGemWeight")
    private String totalGemWeight;

    @XStreamAlias(value="ns2:IsAdultProduct")
    private String isAdultProduct;

    @XStreamAlias(value="ns2:BackFinding")
    private String backFinding;

    @XStreamAlias(value="ns2:Size")
    private String size;



}
