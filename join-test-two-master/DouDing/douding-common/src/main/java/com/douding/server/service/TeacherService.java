package com.douding.server.service;

import com.douding.server.domain.Teacher;
import com.douding.server.domain.TeacherExample;
import com.douding.server.dto.PageDto;
import com.douding.server.dto.TeacherDto;
import com.douding.server.mapper.TeacherMapper;
import com.douding.server.util.CopyUtil;
import com.douding.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;


    /**
     * 列表查询
     */
    public void list(PageDto pageDto) {
        PageHelper.startPage(pageDto.getPage(), pageDto.getSize());
        TeacherExample teacherExample = new TeacherExample();
        List<Teacher> teacherList = teacherMapper.selectByExample(teacherExample);
        PageInfo<Teacher> pageInfo = new PageInfo<>(teacherList);
        pageDto.setTotal(pageInfo.getTotal());
        List<TeacherDto> teacherDtoList = CopyUtil.copyList(teacherList, TeacherDto.class);
        pageDto.setList(teacherDtoList);
    }

    public void save(TeacherDto teacherDto) {
        Teacher teacher = CopyUtil.copy(teacherDto, Teacher.class);

        //判断是新增 还是修改
        if (StringUtils.isEmpty(teacherDto.getId())) {
            this.insert(teacher);
        } else {
            this.update(teacher);
        }



    }

    //新增数据
    private void insert(Teacher teacher) {
        teacher.setId(UuidUtil.getShortUuid());
        teacherMapper.insert(teacher);
    }

    //更新数据
    private void update(Teacher teacher) {
       teacherMapper.updateByPrimaryKey(teacher);
    }
    /*
           删除父分类 同时删除子分类 两个表操作需要开启事务
        */
    @Transactional
    public void delete(String id) {
        teacherMapper.deleteByPrimaryKey(id);
    }
//    public void deleteChildren(String id){
//        Teacher teacher = teacherMapper.selectByPrimaryKey(id);
//        if("00000000".equals(teacher.getId())){
//            //如果是一级分类 需要删除下面的所有二级分类
//            TeacherExample teacherExample = new TeacherExample();
//            teacherExample.createCriteria().andIdNotEqualTo(teacher.getId());
//            teacherMapper.deleteByExample(teacherExample);
//        }


    public List<TeacherDto> all() {
        TeacherExample teacherExample = new TeacherExample();
        teacherExample.setOrderByClause("sort asc");
        List<Teacher> teacheryList = teacherMapper.selectByExample(teacherExample);
        List<TeacherDto> teacherDtoList = CopyUtil.copyList(teacheryList, TeacherDto.class);
        return teacherDtoList;
    }


    /**
     * 查找
     * @param id
     */
    public TeacherDto findById(String id) {
        Teacher teacher = teacherMapper.selectByPrimaryKey(id);
        TeacherDto teacherDto = new TeacherDto();
        BeanUtils.copyProperties(teacher,teacherDto);
        return teacherDto;
    }
}//end class
