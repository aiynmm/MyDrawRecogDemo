package com.sinosoft.mydrawrectdemo.network;

/**
 * Created by Admin on 2016/12/5.
 */

public class Res {
    private String ExternalNo;//外字编码：16ZH:5120
    private String MidNo;//中间字编码：0623A3
    private String CharType;//字符
    private String IDS;//IDS
    private String ImageName;//所在图像文件名：ZHXHP001061-000083
    private String TitileName;//正题名：中國倫理政治大綱
    private String StokeNum;//总笔画数：32
    private String StokeOrder;//全笔顺：25121252511251212512125251125121

    private String StandardRadical;//规范主部首
    private String AttachRadical;//规范附型部首
    private String AttachRadicalNum;//规范部首外笔画数：14
    private String AttachRadicalOrder;//规范部首外笔顺：252511251212512125251125121

    private String KangRadical;//康熙主部首
    private String KangAttachRadical;//康熙附型部首
    private String KangAttachRadicalNum;//康熙部首外笔画数：14
    private String KangAttachRadicalOrder;//康熙部首外笔顺：252511251212512125251125121

    private String Pinyin;//汉语拼音：bin4
    private String Paraphrase;//释义："同“儐”，陳列。
    private String RelatedWord;//关系字：儐
    private String Relations;//字际关系
    private String Examples;//例证：xxxxxxxxx
    private String ExampleFrom;//例证出处

    public String getStandardRadical() {
        return StandardRadical;
    }

    public void setStandardRadical(String standardRadical) {
        StandardRadical = standardRadical;
    }

    public String getRelations() {
        return Relations;
    }

    public void setRelations(String relations) {
        Relations = relations;
    }

    public String getKangAttachRadicalNum() {
        return KangAttachRadicalNum;
    }

    public void setKangAttachRadicalNum(String kangAttachRadicalNum) {
        KangAttachRadicalNum = kangAttachRadicalNum;
    }

    public String getMidNo() {
        return MidNo;
    }

    public void setMidNo(String midNo) {
        MidNo = midNo;
    }

    public String getPinyin() {
        return Pinyin;
    }

    public void setPinyin(String pinyin) {
        Pinyin = pinyin;
    }

    public String getKangRadical() {
        return KangRadical;
    }

    public void setKangRadical(String kangRadical) {
        KangRadical = kangRadical;
    }

    public String getAttachRadicalOrder() {
        return AttachRadicalOrder;
    }

    public void setAttachRadicalOrder(String attachRadicalOrder) {
        AttachRadicalOrder = attachRadicalOrder;
    }

    public String getIDS() {
        return IDS;
    }

    public void setIDS(String IDS) {
        this.IDS = IDS;
    }

    public String getExternalNo() {
        return ExternalNo;
    }

    public void setExternalNo(String externalNo) {
        ExternalNo = externalNo;
    }

    public String getKangAttachRadical() {
        return KangAttachRadical;
    }

    public void setKangAttachRadical(String kangAttachRadical) {
        KangAttachRadical = kangAttachRadical;
    }

    public String getAttachRadical() {
        return AttachRadical;
    }

    public void setAttachRadical(String attachRadical) {
        AttachRadical = attachRadical;
    }

    public String getImageName() {
        return ImageName;
    }

    public void setImageName(String imageName) {
        ImageName = imageName;
    }

    public String getCharType() {
        return CharType;
    }

    public void setCharType(String charType) {
        CharType = charType;
    }

    public String getKangAttachRadicalOrder() {
        return KangAttachRadicalOrder;
    }

    public void setKangAttachRadicalOrder(String kangAttachRadicalOrder) {
        KangAttachRadicalOrder = kangAttachRadicalOrder;
    }

    public String getRelatedWord() {
        return RelatedWord;
    }

    public void setRelatedWord(String relatedWord) {
        RelatedWord = relatedWord;
    }

    public String getAttachRadicalNum() {
        return AttachRadicalNum;
    }

    public void setAttachRadicalNum(String attachRadicalNum) {
        AttachRadicalNum = attachRadicalNum;
    }

    public String getStokeNum() {
        return StokeNum;
    }

    public void setStokeNum(String stokeNum) {
        StokeNum = stokeNum;
    }

    public String getTitileName() {
        return TitileName;
    }

    public void setTitileName(String titileName) {
        TitileName = titileName;
    }

    public String getParaphrase() {
        return Paraphrase;
    }

    public void setParaphrase(String paraphrase) {
        Paraphrase = paraphrase;
    }

    public String getStokeOrder() {
        return StokeOrder;
    }

    public void setStokeOrder(String stokeOrder) {
        StokeOrder = stokeOrder;
    }

    public String getExamples() {
        return Examples;
    }

    public void setExamples(String examples) {
        Examples = examples;
    }

    public String getExampleFrom() {
        return ExampleFrom;
    }

    public void setExampleFrom(String exampleFrom) {
        ExampleFrom = exampleFrom;
    }
}
