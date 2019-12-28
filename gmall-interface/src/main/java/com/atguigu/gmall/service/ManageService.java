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
     * @param baseCatalog2
     * @return
     */
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);

    /**
     * 根据二级分类Id获取三级分类数据
     * @param baseCatalog3
     * @return
     */
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);

    /**
     * 根据三级分类Id获取属性
     * @param baseAttrInfo
     * @return
     */
    public List<BaseAttrInfo> getAttrList(BaseAttrInfo baseAttrInfo);

    /**
     * 如果Id不存在：保存属性&属性值。
     * 如果Id存在：
     *      如果name和属性值不存在：删除属性&属性值
     *      如果name和属性值存在：修改属性&属性值
     * @param baseAttrInfo
     */
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据attrId获取属性
     * @param attrId
     * @return
     */
    public BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 根据attrId获取属性值
     * @param attrId
     * @return
     */
    public List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 通过三级分类Id 查询
     * @param spuInfo
     * @return
     */
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

}
