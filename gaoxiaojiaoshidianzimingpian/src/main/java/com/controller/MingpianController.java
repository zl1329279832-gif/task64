
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 名片
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/mingpian")
public class MingpianController {
    private static final Logger logger = LoggerFactory.getLogger(MingpianController.class);

    @Autowired
    private MingpianService mingpianService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private JiaoshiService jiaoshiService;

    @Autowired
    private XueshengService xueshengService;

    //版本历史service
    @Autowired
    private MingpianHistoryService mingpianHistoryService;

    //科研成果service
    @Autowired
    private KeyanchengguoService keyanchengguoService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学生".equals(role))
            params.put("xueshengId",request.getSession().getAttribute("userId"));
        else if("教师".equals(role))
            params.put("jiaoshiId",request.getSession().getAttribute("userId"));
//        params.put("mingpianDeleteStart",1);params.put("mingpianDeleteEnd",1);
        //前端列表默认只显示已审核上架的名片
        if(params.get("shangxiaTypes") == null || "".equals(params.get("shangxiaTypes"))){
            params.put("shangxiaTypes", 1);
        }
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = mingpianService.queryPage(params);

        //字典表数据转换
        List<MingpianView> list =(List<MingpianView>)page.getList();
        for(MingpianView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        MingpianEntity mingpian = mingpianService.selectById(id);
        if(mingpian !=null){
            //entity转view
            MingpianView view = new MingpianView();
            BeanUtils.copyProperties( mingpian , view );//把实体数据重构到view中

                //级联表
                JiaoshiEntity jiaoshi = jiaoshiService.selectById(mingpian.getJiaoshiId());
                if(jiaoshi != null){
                    BeanUtils.copyProperties( jiaoshi , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setJiaoshiId(jiaoshi.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody MingpianEntity mingpian, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,mingpian:{}",this.getClass().getName(),mingpian.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("教师".equals(role))
            mingpian.setJiaoshiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<MingpianEntity> queryWrapper = new EntityWrapper<MingpianEntity>()
            .eq("jiaoshi_id", mingpian.getJiaoshiId())
            .eq("mingpian_delete", 1)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        MingpianEntity mingpianEntity = mingpianService.selectOne(queryWrapper);
        if(mingpianEntity==null){
            mingpian.setMingpianClicknum(1);
            mingpian.setShangxiaTypes(1);
            mingpian.setMingpianDelete(1);
            mingpian.setCreateTime(new Date());
            mingpianService.insert(mingpian);
            return R.ok();
        }else {
            return R.error(511,"该老师已有名片");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody MingpianEntity mingpian, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,mingpian:{}",this.getClass().getName(),mingpian.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("教师".equals(role))
//            mingpian.setJiaoshiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<MingpianEntity> queryWrapper = new EntityWrapper<MingpianEntity>()
            .notIn("id",mingpian.getId())
            .andNew()
            .eq("jiaoshi_id", mingpian.getJiaoshiId())
            .eq("mingpian_delete", 1)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        MingpianEntity mingpianEntity = mingpianService.selectOne(queryWrapper);
        if("".equals(mingpian.getMingpianFile()) || "null".equals(mingpian.getMingpianFile())){
                mingpian.setMingpianFile(null);
        }
        if("".equals(mingpian.getMingpianPhoto()) || "null".equals(mingpian.getMingpianPhoto())){
                mingpian.setMingpianPhoto(null);
        }
        if(mingpianEntity==null){
            mingpianService.updateById(mingpian);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"该老师已有名片");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<MingpianEntity> list = new ArrayList<>();
        for(Integer id:ids){
            MingpianEntity mingpianEntity = new MingpianEntity();
            mingpianEntity.setId(id);
            mingpianEntity.setMingpianDelete(2);
            list.add(mingpianEntity);
        }
        if(list != null && list.size() >0){
            mingpianService.updateBatchById(list);
        }
        return R.ok();
    }



    /**
    * 删除1
    */
    @RequestMapping("/delete1")
    public R delete1(@RequestBody Integer[] ids){
        logger.debug("delete1:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        mingpianService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<MingpianEntity> mingpianList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            MingpianEntity mingpianEntity = new MingpianEntity();
//                            mingpianEntity.setJiaoshiId(Integer.valueOf(data.get(0)));   //教师 要改的
//                            mingpianEntity.setMingpianName(data.get(0));                    //名片名称 要改的
//                            mingpianEntity.setMingpianUuidNumber(data.get(0));                    //名片编号 要改的
//                            mingpianEntity.setMingpianXingming(data.get(0));                    //姓名 要改的
//                            mingpianEntity.setMingpianPhone(data.get(0));                    //联系电话 要改的
//                            mingpianEntity.setMingpianFile(data.get(0));                    //名片文件 要改的
//                            mingpianEntity.setSexTypes(Integer.valueOf(data.get(0)));   //性别 要改的
//                            mingpianEntity.setZhiwuTypes(Integer.valueOf(data.get(0)));   //职务 要改的
//                            mingpianEntity.setMingpianPhoto("");//详情和图片
//                            mingpianEntity.setMingpianTypes(Integer.valueOf(data.get(0)));   //名片类型 要改的
//                            mingpianEntity.setXueyuanTypes(Integer.valueOf(data.get(0)));   //学院 要改的
//                            mingpianEntity.setBangongshiTypes(Integer.valueOf(data.get(0)));   //办公室 要改的
//                            mingpianEntity.setKechengTypes(Integer.valueOf(data.get(0)));   //主修课程 要改的
//                            mingpianEntity.setMingpianClicknum(Integer.valueOf(data.get(0)));   //名片热度 要改的
//                            mingpianEntity.setMingpianContent("");//详情和图片
//                            mingpianEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否展示 要改的
//                            mingpianEntity.setMingpianDelete(1);//逻辑删除字段
//                            mingpianEntity.setCreateTime(date);//时间
                            mingpianList.add(mingpianEntity);


                            //把要查询是否重复的字段放入map中
                                //名片编号
                                if(seachFields.containsKey("mingpianUuidNumber")){
                                    List<String> mingpianUuidNumber = seachFields.get("mingpianUuidNumber");
                                    mingpianUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> mingpianUuidNumber = new ArrayList<>();
                                    mingpianUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("mingpianUuidNumber",mingpianUuidNumber);
                                }
                                //联系电话
                                if(seachFields.containsKey("mingpianPhone")){
                                    List<String> mingpianPhone = seachFields.get("mingpianPhone");
                                    mingpianPhone.add(data.get(0));//要改的
                                }else{
                                    List<String> mingpianPhone = new ArrayList<>();
                                    mingpianPhone.add(data.get(0));//要改的
                                    seachFields.put("mingpianPhone",mingpianPhone);
                                }
                        }

                        //查询是否重复
                         //名片编号
                        List<MingpianEntity> mingpianEntities_mingpianUuidNumber = mingpianService.selectList(new EntityWrapper<MingpianEntity>().in("mingpian_uuid_number", seachFields.get("mingpianUuidNumber")).eq("mingpian_delete", 1));
                        if(mingpianEntities_mingpianUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(MingpianEntity s:mingpianEntities_mingpianUuidNumber){
                                repeatFields.add(s.getMingpianUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [名片编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                         //联系电话
                        List<MingpianEntity> mingpianEntities_mingpianPhone = mingpianService.selectList(new EntityWrapper<MingpianEntity>().in("mingpian_phone", seachFields.get("mingpianPhone")).eq("mingpian_delete", 1));
                        if(mingpianEntities_mingpianPhone.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(MingpianEntity s:mingpianEntities_mingpianPhone){
                                repeatFields.add(s.getMingpianPhone());
                            }
                            return R.error(511,"数据库的该表中的 [联系电话] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        mingpianService.insertBatch(mingpianList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        //前端列表默认只显示已审核上架的名片
        if(params.get("shangxiaTypes") == null || "".equals(params.get("shangxiaTypes"))){
            params.put("shangxiaTypes", 1);
        }

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = mingpianService.queryPage(params);

        //字典表数据转换
        List<MingpianView> list =(List<MingpianView>)page.getList();
        for(MingpianView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        MingpianEntity mingpian = mingpianService.selectById(id);
            if(mingpian !=null){

                //点击数量原子递增，避免并发 read-modify-write 竞态
                mingpianService.incrementClickNum(mingpian.getId());

                //重新读取以获取更新后的点击数
                mingpian = mingpianService.selectById(id);

                //entity转view
                MingpianView view = new MingpianView();
                BeanUtils.copyProperties( mingpian , view );//把实体数据重构到view中

                //级联表
                    JiaoshiEntity jiaoshi = jiaoshiService.selectById(mingpian.getJiaoshiId());
                if(jiaoshi != null){
                    BeanUtils.copyProperties( jiaoshi , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setJiaoshiId(jiaoshi.getId());
                }

                //聚合该教师的已上架科研成果
                List<KeyanchengguoEntity> kcgList = keyanchengguoService.selectList(
                    new EntityWrapper<KeyanchengguoEntity>()
                        .eq("jiaoshi_id", mingpian.getJiaoshiId())
                        .eq("keyanchengguo_delete", 1)
                        .eq("shangxia_types", 1)
                );
                List<KeyanchengguoView> kcgViews = new ArrayList<>();
                for(KeyanchengguoEntity kcg : kcgList){
                    KeyanchengguoView kv = new KeyanchengguoView();
                    BeanUtils.copyProperties(kcg, kv);
                    dictionaryService.dictionaryConvert(kv, request);
                    kcgViews.add(kv);
                }
                view.setKeyanchengguoList(kcgViews);

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody MingpianEntity mingpian, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,mingpian:{}",this.getClass().getName(),mingpian.toString());
        Wrapper<MingpianEntity> queryWrapper = new EntityWrapper<MingpianEntity>()
            .eq("jiaoshi_id", mingpian.getJiaoshiId())
            .eq("mingpian_name", mingpian.getMingpianName())
            .eq("mingpian_uuid_number", mingpian.getMingpianUuidNumber())
            .eq("mingpian_xingming", mingpian.getMingpianXingming())
            .eq("mingpian_phone", mingpian.getMingpianPhone())
            .eq("sex_types", mingpian.getSexTypes())
            .eq("zhiwu_types", mingpian.getZhiwuTypes())
            .eq("mingpian_types", mingpian.getMingpianTypes())
            .eq("xueyuan_types", mingpian.getXueyuanTypes())
            .eq("bangongshi_types", mingpian.getBangongshiTypes())
            .eq("kecheng_types", mingpian.getKechengTypes())
            .eq("mingpian_clicknum", mingpian.getMingpianClicknum())
            .eq("shangxia_types", mingpian.getShangxiaTypes())
            .eq("mingpian_delete", mingpian.getMingpianDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        MingpianEntity mingpianEntity = mingpianService.selectOne(queryWrapper);
        if(mingpianEntity==null){
            mingpian.setMingpianDelete(1);
            mingpian.setCreateTime(new Date());
        mingpianService.insert(mingpian);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
     * 教师提交新版名片（待审核）
     */
    @RequestMapping("/submitVersion")
    public R submitVersion(@RequestBody MingpianEntity mingpian, HttpServletRequest request){
        logger.debug("submitVersion方法:,,Controller:{},,mingpian:{}",this.getClass().getName(),mingpian.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(!"教师".equals(role))
            return R.error(511,"只有教师可以提交名片版本");

        Integer jiaoshiId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        mingpian.setJiaoshiId(jiaoshiId);

        //校验是否已有待审核版本
        MingpianEntity pending = mingpianService.selectOne(
            new EntityWrapper<MingpianEntity>()
                .eq("jiaoshi_id", jiaoshiId)
                .eq("mingpian_delete", 1)
                .eq("shangxia_types", 2)
        );
        if(pending != null)
            return R.error(511,"您已有一个待审核的名片版本，请等待审核完成");

        //确定版本号：当前最大版本号+1
        MingpianEntity active = mingpianService.selectOne(
            new EntityWrapper<MingpianEntity>()
                .eq("jiaoshi_id", jiaoshiId)
                .eq("mingpian_delete", 1)
                .eq("shangxia_types", 1)
        );
        mingpian.setVersionNumber(active != null ? active.getVersionNumber() + 1 : 1);

        mingpian.setShangxiaTypes(2);      //待审核
        mingpian.setMingpianDelete(1);
        mingpian.setMingpianClicknum(1);
        mingpian.setCreateTime(new Date());
        mingpianService.insert(mingpian);
        return R.ok().put("data", mingpian);
    }


    /**
     * 管理员审核通过名片版本
     */
    @RequestMapping("/approveVersion")
    public R approveVersion(@RequestParam("id") Integer id, HttpServletRequest request){
        logger.debug("approveVersion方法:,,Controller:{},,id:{}",this.getClass().getName(),id);

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(!"管理员".equals(role))
            return R.error(511,"只有管理员可以审核名片");

        MingpianEntity pendingCard = mingpianService.selectById(id);
        if(pendingCard == null) return R.error(511,"找不到该名片");
        if(pendingCard.getShangxiaTypes() != 2)
            return R.error(511,"该名片不是待审核状态");

        Integer jiaoshiId = pendingCard.getJiaoshiId();

        //找到当前活跃名片并归档
        MingpianEntity activeCard = mingpianService.selectOne(
            new EntityWrapper<MingpianEntity>()
                .eq("jiaoshi_id", jiaoshiId)
                .eq("mingpian_delete", 1)
                .eq("shangxia_types", 1)
        );

        if(activeCard != null){
            //快照到历史表
            MingpianHistoryEntity history = new MingpianHistoryEntity();
            BeanUtils.copyProperties(activeCard, history, new String[]{"id"});
            history.setMingpianId(activeCard.getId());
            history.setSnapshotTime(new Date());
            history.setSnapshotReason("被新版本V" + pendingCard.getVersionNumber() + "替代");
            mingpianHistoryService.insert(history);

            //旧版归档
            activeCard.setShangxiaTypes(4);
            mingpianService.updateById(activeCard);
        }

        //新版上架
        pendingCard.setShangxiaTypes(1);
        mingpianService.updateById(pendingCard);
        return R.ok();
    }


    /**
     * 管理员驳回名片版本
     */
    @RequestMapping("/rejectVersion")
    public R rejectVersion(@RequestParam("id") Integer id,
                           @RequestParam(value = "reason", required = false) String reason,
                           HttpServletRequest request){
        logger.debug("rejectVersion方法:,,Controller:{},,id:{}",this.getClass().getName(),id);

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(!"管理员".equals(role))
            return R.error(511,"只有管理员可以审核名片");

        MingpianEntity card = mingpianService.selectById(id);
        if(card == null) return R.error(511,"找不到该名片");
        if(card.getShangxiaTypes() != 2)
            return R.error(511,"该名片不是待审核状态");

        //记录驳回快照到历史表
        MingpianHistoryEntity history = new MingpianHistoryEntity();
        BeanUtils.copyProperties(card, history, new String[]{"id"});
        history.setMingpianId(card.getId());
        history.setShangxiaTypes(3);
        history.setSnapshotTime(new Date());
        history.setSnapshotReason("审核驳回" + (reason != null ? ": " + reason : ""));
        mingpianHistoryService.insert(history);

        //软删被驳回的名片
        card.setMingpianDelete(2);
        mingpianService.updateById(card);
        return R.ok();
    }


    /**
     * 查询名片历史版本列表
     */
    @RequestMapping("/listHistory")
    public R listHistory(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("listHistory方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if("教师".equals(role))
            params.put("jiaoshiId", request.getSession().getAttribute("userId"));

        PageUtils page = mingpianHistoryService.queryPage(params);
        List<MingpianHistoryView> list = (List<MingpianHistoryView>) page.getList();
        for(MingpianHistoryView v : list)
            dictionaryService.dictionaryConvert(v, request);
        return R.ok().put("data", page);
    }


    /**
     * 回退到指定历史版本
     */
    @RequestMapping("/huitui")
    public R huitui(@RequestParam("historyId") Integer historyId, HttpServletRequest request){
        logger.debug("huitui方法:,,Controller:{},,historyId:{}",this.getClass().getName(),historyId);

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(!"管理员".equals(role) && !"教师".equals(role))
            return R.error(511,"没有权限执行此操作");

        MingpianHistoryEntity history = mingpianHistoryService.selectById(historyId);
        if(history == null)
            return R.error(511,"找不到该历史版本");

        Integer jiaoshiId = history.getJiaoshiId();

        //教师只能回退自己的名片
        if("教师".equals(role)){
            Integer userId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
            if(!jiaoshiId.equals(userId))
                return R.error(511,"只能回退自己的名片");
        }

        //找到当前活跃名片
        MingpianEntity activeCard = mingpianService.selectOne(
            new EntityWrapper<MingpianEntity>()
                .eq("jiaoshi_id", jiaoshiId)
                .eq("mingpian_delete", 1)
                .eq("shangxia_types", 1)
        );

        if(activeCard != null){
            //防止回退到已经是当前活跃的版本
            if(activeCard.getId().equals(history.getMingpianId())
                && activeCard.getVersionNumber().equals(history.getVersionNumber()))
                return R.error(511,"该版本已经是当前活跃版本");

            //当前活跃版归档到历史
            MingpianHistoryEntity archive = new MingpianHistoryEntity();
            BeanUtils.copyProperties(activeCard, archive, new String[]{"id"});
            archive.setMingpianId(activeCard.getId());
            archive.setSnapshotTime(new Date());
            archive.setSnapshotReason("回退到V" + history.getVersionNumber() + "时被归档");
            mingpianHistoryService.insert(archive);

            //旧活跃版标记为归档
            activeCard.setShangxiaTypes(4);
            mingpianService.updateById(activeCard);
        }

        //从历史快照恢复到主表
        MingpianEntity restored = mingpianService.selectById(history.getMingpianId());
        if(restored == null)
            restored = new MingpianEntity();

        BeanUtils.copyProperties(history, restored,
            new String[]{"id", "mingpianId", "snapshotTime", "snapshotReason", "createTime"});
        restored.setId(history.getMingpianId());
        restored.setShangxiaTypes(1);   //上架
        restored.setMingpianDelete(1);  //未删除
        mingpianService.updateById(restored);
        return R.ok();
    }


}
