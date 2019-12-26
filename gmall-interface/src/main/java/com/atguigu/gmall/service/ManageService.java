package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 获取一级分类数据
     * @return
     */
    public List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类Id获取二级分类数据
     * @param catalog1Id
     * @return
     */
    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类Id获取三级分类数据
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类Id获取属性
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 如果Id不存在：保存属性&属性值。
     * 如果Id存在：
     *      如果name和属性值不存在：删除属性&属性值
     *      如果name和属性值存在：修改属性&属性值
     * @param baseAttrInfo
     */
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据attrId获取属性&属性值
     * @param attrId
     * @return
     */
    public BaseAttrInfo getAttrInfo(String attrId);

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

}
