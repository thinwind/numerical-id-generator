# numerical-id-generator
数值型id生成器

## 使用方法

---------

0. 在项目根目录下创建id-gen.properties,一般与log4j.properties在同一目录

   创建后添加两个参数，可以参见测试[demo](src/test/resources/id-gen.properties)

1. 修改[id-gen.properties](src/test/resources/id-gen.properties)中的参数
    
    参数 | 含义
    ---- | ----
    datacenter_id | 数据中心id
    worder_id |  生成器id
    
2. 使用数据中心及生成器id进行区分的id产生方法

    直接调用[IdGenerator](src/main/java/com/github/shang/generator/IdGenerator.java)的`nextId()`方法即可
    
  *注意：为了最大限度保证ID的唯一性，不同的机器，不同的生成线程，建议使用不同的datacenter_id和worder_id*
  
3. 使用用户标识，进行逻辑分片产生ID

  调用[IdGenerator](src/main/java/com/github/shang/generator/IdGenerator.java)的`nextId(long tag)`方法，tag为用户的long类型的标识
  


        
        
           
           

## 原理介绍

--------

**[数值型ID生成器](http://www.shangyh.win/java/2016/12/24/java-%E5%AE%9E%E7%8E%B0%E6%95%B0%E5%80%BC%E5%9E%8B-id-%E7%94%9F%E6%88%90%E6%96%B9%E6%A1%88.html)**


