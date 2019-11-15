package com.zijin.music.musicvote.utils;


import com.alibaba.fastjson.util.TypeUtils;
import com.zijin.music.musicvote.anno.EnableExport;
import com.zijin.music.musicvote.anno.EnableExportField;
import com.zijin.music.musicvote.anno.EnableSelectList;
import com.zijin.music.musicvote.anno.ImportIndex;
import com.zijin.music.musicvote.bean.ColorEnum;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel 通用导入导出工具
 * @author jenkin
 * @version 1.0
 */
public class ExcelUtils {
    private static Pattern pattren = Pattern.compile("[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");

    /**  所有的下拉列表数据存在这个map中，key是对应的Excel列的序号，从0开始，value为下拉列表键对值 **/
    public static final  Map<Integer,Map<String,String>> ALL_SELECT_LIST_MAP = new HashMap<Integer,Map<String,String>> ();
        /**
         * 将Excel转换为对象集合
         * @param excel Excel 文件
         * @param clazz pojo类型
         * @return
         */
        public static <T> List<T> parseExcelToList(File excel, Class<T> clazz){
            List<Object> res = new ArrayList<Object>();
            // 创建输入流，读取Excel
            InputStream is = null;
            Sheet sheet = null;
            try {
                is = new FileInputStream(excel.getAbsolutePath());
                if (is != null) {
                    Workbook workbook = WorkbookFactory.create(is);
                    //默认只获取第一个工作表
                    sheet = workbook.getSheetAt(0);
                    if (sheet != null) {
                        int i = 1;
                        String values[] ;
                        Row row = sheet.getRow(i);
                        while (row != null) {
                            //获取单元格数目
                            int cellNum = row.getPhysicalNumberOfCells();
                            Field[] fields = clazz.getDeclaredFields();
                            if (fields.length>cellNum){
                                throw new RuntimeException("导入失败！请确认第"+(i+1)+"行或其他行、列表格是否完整！");
                            }
                            values = new String[cellNum];
                            for (int j = 0; j <= cellNum; j++) {
                                Cell cell =   row.getCell(j);
                                if (cell != null) {
                                    //获取单元格值
                                    cell.setCellType(CellType.STRING);
                                    values[j] = cell.getStringCellValue() == null ? null : cell.getStringCellValue();

                                }
                            }
                            Object obj = clazz.newInstance();
                            for(Field f : fields){
                                if(f.isAnnotationPresent(ImportIndex.class)){
                                    ImportIndex annotation = f.getAnnotation(ImportIndex.class);
                                    int index = annotation.index();
                                    String useSetMethodName = annotation.useSetMethodName();
                                    if(!"".equals(useSetMethodName)){
                                        Object val = TypeUtils.cast(values[index],f.getType(),null);
                                        f.setAccessible(true);
                                        Method method = clazz.getMethod(useSetMethodName, new Class[]{f.getType(),Object.class});
                                        method.setAccessible(true);
                                        method.invoke(obj, new Object[]{f.get(obj),val});
                                    }else{
                                        f.setAccessible(true);
                                        Object val = TypeUtils.cast(values[index],f.getType(),null);
                                        f.set(obj,val);
                                    }
                                }
                            }
                            res.add(obj);
                            i++;
                            row=sheet.getRow(i);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (List<T>) res;
        }

        /**
         * 将Excel转换为对象集合
         * @param excel Excel 文件输入流
         * @param clazz pojo类型
         * @return
         */
        public static <T> List<T> parseExcelToList(InputStream excel,Class<T> clazz) throws IOException,
                InvalidFormatException,
                InstantiationException,
                IllegalAccessException,
                NoSuchMethodException,
                InvocationTargetException {
            List<Object> res = new ArrayList<Object>();
            // 创建输入流，读取Excel
            InputStream is = null;
            Sheet sheet = null;

            is = excel;
            if (is != null) {
                Workbook workbook = WorkbookFactory.create(is);
                //默认只获取第一个工作表
                sheet = workbook.getSheetAt(0);
                if (sheet != null) {
                    int i = 1;
                    String values[] ;
                    Row row = sheet.getRow(i);
                    while (row != null) {
                        //获取单元格数目
                        int cellNum = row.getPhysicalNumberOfCells();
                        Field[] fields = clazz.getDeclaredFields();
                        if (fields.length>cellNum){
                            throw new RuntimeException("导入失败！请确认第"+(i+1)+"行或其他行、列表格是否完整！");
                        }
                        values = new String[cellNum];
                        for (int j = 0; j <=cellNum; j++) {
                            Cell cell =   row.getCell(j);
                            if (cell != null) {
                                //获取单元格值
                                String value = null;
                                if (j==5||j==6){
                                    SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd");
                                    try {
                                        double dt = cell.getNumericCellValue();
                                        value = String.valueOf(dt);
                                        if (!StringUtils.isEmpty(value)&&!"0.0".equals(value)) {
                                            Date date = DateUtil.getJavaDate(dt);
                                            value = simpleDateFormat.format(date);

                                        } else {
                                            value = null;
                                        }
                                    }catch (Exception e){
                                        value = null;
                                    }
                                }else{
                                    cell.setCellType(CellType.STRING);
                                    value = cell.getStringCellValue() == null ? null : cell.getStringCellValue();
                                }
                                values[j]=value;
                            }
                        }

                        Object obj = clazz.newInstance();
                        for(Field f : fields){
                            if(f.isAnnotationPresent(ImportIndex.class)){

                                ImportIndex annotation = f.getAnnotation(ImportIndex.class);
                                int index = annotation.index();
                                Object value = values[index];
                                if(f.isAnnotationPresent(EnableSelectList.class)){

                                    value = getKeyByValue(ALL_SELECT_LIST_MAP.get(index),String.valueOf(value ) );

                                }
                                String useSetMethodName = annotation.useSetMethodName();
                                if(!"".equals(useSetMethodName)){
                                    Object val = TypeUtils.cast(value,f.getType(),null);
                                    f.setAccessible(true);
                                    Method method = clazz.getMethod(useSetMethodName, new Class[]{f.getType(),Object.class});
                                    method.setAccessible(true);
                                    method.invoke(obj, new Object[]{f.get(obj),val});
                                }else{
                                    f.setAccessible(true);
                                    Object val = TypeUtils.cast(value,f.getType(),null);
                                    f.set(obj,val);
                                }

                            }
                        }
                        res.add(obj);
                        i++;
                        row=sheet.getRow(i);
                    }
                }
            }

            return (List<T>)res;
        }

        /**
         * 将Excel转换为对象集合
         * @param sheet Excel 文件输入流
         * @param clazz pojo类型
         * @return
         */
        public static List<?> parseExcelToList(Sheet sheet,Class clazz,int startIndex,int endIndex ) throws
                InstantiationException,
                IllegalAccessException,
                NoSuchMethodException,
                InvocationTargetException {
            List<Object> res = new ArrayList<Object>();

                    String values[];

                    for (int i = startIndex; i <= endIndex; i++) {
                        Row row = sheet.getRow(i);
                        //获取单元格数目
//                        int cellNum = row.getPhysicalNumberOfCells();
                        Field[] fields = clazz.getDeclaredFields();
                        int cellNum = fields.length;
                        if (fields.length > cellNum) {
                            break;
                        }
                        values = new String[cellNum];
                        for (int j = 0; j <= cellNum; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                if (j==0||j==1){
                                    cell.setCellType(CellType.STRING);
                                    if (StringUtils.isEmpty(cell.getStringCellValue())){
                                        break;
                                    }
                                }
                                //获取单元格值
                                String value = null;
                                if (j == 5 || j == 6) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    try {
                                        double dt = cell.getNumericCellValue();
                                        value = String.valueOf(dt);
                                        if (!StringUtils.isEmpty(value) && !"0.0".equals(value)) {
                                            Date date = DateUtil.getJavaDate(dt);
                                            value = simpleDateFormat.format(date);

                                        } else {
                                            value = null;
                                        }
                                    } catch (Exception e) {
                                        value = null;
                                    }
                                } else {
                                    cell.setCellType(CellType.STRING);
                                    value = cell.getStringCellValue() == null ? null : cell.getStringCellValue();
                                }
                                values[j] = value;
                            }
                        }

                        if (!StringUtils.isEmpty(values[0])&&!StringUtils.isEmpty(values[1])) {
                            Object obj = clazz.newInstance();
                            for (Field f : fields) {
                                if (f.isAnnotationPresent(ImportIndex.class)) {

                                    ImportIndex annotation = f.getAnnotation(ImportIndex.class);
                                    int index = annotation.index();
                                    Object value = values[index];
                                    if (f.isAnnotationPresent(EnableSelectList.class)) {

                                        value = getKeyByValue(ALL_SELECT_LIST_MAP.get(index), String.valueOf(value));

                                    }
                                    String useSetMethodName = annotation.useSetMethodName();
                                    if (!"".equals(useSetMethodName)) {
                                        Object val = TypeUtils.cast(value, f.getType(), null);
                                        f.setAccessible(true);
                                        Method method = clazz.getMethod(useSetMethodName, new Class[]{f.getType(), Object.class});
                                        method.setAccessible(true);
                                        method.invoke(obj, new Object[]{f.get(obj), val});
                                    } else {
                                        f.setAccessible(true);
                                        Object val = TypeUtils.cast(value, f.getType(), null);
                                        f.set(obj, val);
                                    }

                                }
                            }
                            res.add(obj);
                        }
                    }

            return res;
        }

    /**
     *
     * @param sheet
     * @param clazz
     * @param startIndex
     * @param endIndex
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
        public static List<?> excelToList(Sheet sheet,Class clazz,int startIndex,int endIndex ) throws
                InstantiationException,
                IllegalAccessException,
                NoSuchMethodException,
                InvocationTargetException {
            List<Object> res = new ArrayList<Object>();

                    String values[];

                    for (int i = startIndex; i <= endIndex; i++) {
                        Row row = sheet.getRow(i);
                        //获取单元格数目
                        Field[] fields = clazz.getDeclaredFields();
                        int cellNum = fields.length;
                        if (fields.length > cellNum) {
                            break;
                        }
                        values = new String[cellNum];
                        for (int j = 0; j <= cellNum; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                //获取单元格值
                                cell.setCellType(CellType.STRING);
                                values[j] = cell.getStringCellValue() == null ? null : cell.getStringCellValue();
                            }
                        }

                        if (!StringUtils.isEmpty(values[0])&&!StringUtils.isEmpty(values[1])) {
                            Object obj = clazz.newInstance();
                            for (Field f : fields) {
                                if (f.isAnnotationPresent(ImportIndex.class)) {

                                    ImportIndex annotation = f.getAnnotation(ImportIndex.class);
                                    int index = annotation.index();
                                    Object value = values[index];
                                    if (f.isAnnotationPresent(EnableSelectList.class)) {

                                        value = getKeyByValue(ALL_SELECT_LIST_MAP.get(index), String.valueOf(value));

                                    }
                                    String useSetMethodName = annotation.useSetMethodName();
                                    if (!"".equals(useSetMethodName)) {
                                        Object val = TypeUtils.cast(value, f.getType(), null);
                                        f.setAccessible(true);
                                        Method method = clazz.getMethod(useSetMethodName, new Class[]{f.getType(), Object.class});
                                        method.setAccessible(true);
                                        method.invoke(obj, new Object[]{f.get(obj), val});
                                    } else {
                                        f.setAccessible(true);
                                        Object val = TypeUtils.cast(value, f.getType(), null);
                                        f.set(obj, val);
                                    }

                                }
                            }
                            res.add(obj);
                        }
                    }

            return res;
        }

    /**
     *
     * @param excel
     * @param clazz
     * @param startIndex
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static List<?> parseExcelToList(InputStream excel,Class clazz,int startIndex ,int startCol ) throws
            InstantiationException,
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException {
        List<Object> res = new ArrayList<Object>();
        // 创建输入流，读取Excel
        InputStream is = null;
        Sheet sheet = null;

        is = excel;
        if (is != null) {
            Workbook workbook = null;
            try {
                workbook = WorkbookFactory.create(is);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            //默认只获取第一个工作表
            sheet = workbook.getSheetAt(0);
            if (sheet != null) {
                int i = startIndex;
                int cellNum = sheet.getRow((i-1)>0?(i-1):i).getPhysicalNumberOfCells();
                String values[] ;
                Row row = sheet.getRow(i);
                while (row != null) {
                    //获取单元格数目

                    Field[] fields = clazz.getDeclaredFields();
                   // System.out.println("一共有 "+cellNum+" 列");
                    values = new String[cellNum+startCol];
                    for (int j =startCol; j <cellNum; j++) {
                        Cell cell =   row.getCell(j);
                        if (cell != null) {
                            //获取单元格值
                            String value = null;
//                            if (j==5||j==6){
//                                SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd");
//                                try {
//                                    double dt = cell.getNumericCellValue();
//                                    value = String.valueOf(dt);
//                                    if (!StringUtils.isEmpty(value)&&!"0.0".equals(value)) {
//                                        Date date = DateUtil.getJavaDate(dt);
//                                        value = simpleDateFormat.format(date);
//
//                                    } else {
//                                        value = null;
//                                    }
//                                }catch (Exception e){
//                                    value = null;
//                                }
//                            }else{
                                cell.setCellType(CellType.STRING);
                                value = cell.getStringCellValue() == null ? null : cell.getStringCellValue();
                          //  }
                            values[j]=value;
                        }
                    }

                    Object obj = clazz.newInstance();
                    for(Field f : fields){
                        if(f.isAnnotationPresent(ImportIndex.class)){

                            ImportIndex annotation = f.getAnnotation(ImportIndex.class);
                            int index = annotation.index();
                            Object value = values[index];
                            if(f.isAnnotationPresent(EnableSelectList.class)){

                                value = getKeyByValue(ALL_SELECT_LIST_MAP.get(index),String.valueOf(value ) );

                            }
                            String useSetMethodName = annotation.useSetMethodName();
                            if(!"".equals(useSetMethodName)){
                                Object val = TypeUtils.cast(value,f.getType(),null);
                                f.setAccessible(true);
                                Method method = clazz.getMethod(useSetMethodName, new Class[]{f.getType(),Object.class});
                                method.setAccessible(true);
                                method.invoke(obj, new Object[]{f.get(obj),val});
                            }else{
                                f.setAccessible(true);
                                Object val = TypeUtils.cast(value,f.getType(),null);
                                f.set(obj,val);
                            }

                        }
                    }
                    res.add(obj);
                    i++;
                    row=sheet.getRow(i);
                }
            }
        }

        return res;
    }



        /**
         * 导出 Excel
         * @param outputStream 输出流，用于写文件
         * @param dataList 需要导出的数据
         * @param clazz 导出数据的pojo类型
         * @param selectListMap 下拉列表的列
         * @param exportTitle 当该参数不为空则替换默认的标题
         */
        public static void exportExcel(OutputStream outputStream, List  dataList, Class clazz, Map<Integer,Map<String,String>> selectListMap,String exportTitle){
            //创建一个Excel工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            //建立表
            HSSFSheet hssfsheet =  workbook.createSheet();

            hssfsheet.setDefaultRowHeight( ( short )(20*20) );
            //检查当前pojo是否允许导出
            if(clazz.isAnnotationPresent(EnableExport.class)) {
                EnableExport export = (EnableExport) clazz.getAnnotation(EnableExport.class);
                //获取所有标题名称
                List<String> colNames =new ArrayList<String>();
                //获取所有标题的背景颜色
                List<ColorEnum> colors =new ArrayList<ColorEnum>();
                //所有允许导出的字段
                List<Field> fieldList = new ArrayList<Field>();
                for(Field field : clazz.getDeclaredFields()){
                    if(field.isAnnotationPresent(EnableExportField.class)){
                        EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                        colNames.add(enableExportField.colName());
                        colors.add(enableExportField.cellColor());
                        fieldList.add(field);
                    }
                }
                //设置每列的宽度
                for(int i=0;i<fieldList.size();i++){
                    Field field = fieldList.get(i);
                    hssfsheet.setColumnWidth(i,field.getAnnotation(EnableExportField.class).colWidth()*20);
                }

                HSSFRow hssfRow = null;
                HSSFCell hssfcell = null;

                //绘制表头以及菜单
                String fileName =export.fileName();
                if(exportTitle!=null){
                    fileName = exportTitle;
                }

                //创建标题行（表头）
                createHeadRow(workbook,hssfRow,hssfcell,hssfsheet,colNames,colors);
                try {

                    //插入内容
                    int i=0;
                    for (Object obj : dataList) {
                        hssfRow = hssfsheet.createRow(i + 1);
                        //设置每列的宽度
                        //此处设置j=-1 ：添加一列，序号列
                        for(int j=0;j<fieldList.size();j++){

                            Field field = fieldList.get(j);
                            field.setAccessible(true);
                            Object value = field.get(obj);
                            EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                            String getMethodName = enableExportField.useGetMethod();
                            if(!"".equals( getMethodName )){
                                Method  method = clazz.getMethod(getMethodName, new Class[]{field.getType()});
                                method.setAccessible(true);
                                value= method.invoke(obj, new Object[]{value});
                            }
                            if(field.isAnnotationPresent(EnableSelectList.class)){
                                if(selectListMap!=null&& selectListMap.get(j)!=null) {
                                    value = selectListMap.get(j).get(value);
                                }
                            }
                            hssfcell = hssfRow.createCell(j);
                            if(j==5||j==6){
                                if (value!=null) {
                                    hssfcell.setCellValue((Date) value);
                                }else {
                                    hssfcell.setCellValue("");
                                }
                                CreationHelper creationHelper = workbook.getCreationHelper();
                                CellStyle cellStyle = workbook.createCellStyle();//新建单元格样式
                                cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));//单元格样式模板
                                hssfcell.setCellStyle(cellStyle);//设置单元格样式
                            }else {
                                hssfcell.setCellValue(String.valueOf(value));
                            }
                        }
                        i++;
                    }
                    //创建下拉列表
                    createDataValidation(hssfsheet,selectListMap);
                    workbook.write(outputStream);

                } catch (IllegalAccessException e ) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 导出 Excel
         * @param outputStream 输出流，用于写文件
         * @param dataList 需要导出的数据
         * @param clazz 导出数据的pojo类型
         * @param selectListMap 下拉列表的列
         * @param exportTitle 当该参数不为空则替换默认的标题
         */
        public static void exportExcelHis(OutputStream outputStream, List  dataList, Class clazz, Map<Integer,Map<String,String>> selectListMap,String exportTitle){
            //创建一个Excel工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            //建立表
            HSSFSheet hssfsheet =  workbook.createSheet();

            hssfsheet.setDefaultRowHeight( ( short )(20*20) );
            //检查当前pojo是否允许导出
            if(clazz.isAnnotationPresent(EnableExport.class)) {
                EnableExport export = (EnableExport) clazz.getAnnotation(EnableExport.class);
                //获取所有标题名称
                List<String> colNames =new ArrayList<String>();
                //获取所有标题的背景颜色
                List<ColorEnum> colors =new ArrayList<ColorEnum>();
                //所有允许导出的字段
                List<Field> fieldList = new ArrayList<Field>();
                for(Field field : clazz.getDeclaredFields()){
                    if(field.isAnnotationPresent(EnableExportField.class)){
                        EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                        colNames.add(enableExportField.colName());
                        colors.add(enableExportField.cellColor());
                        fieldList.add(field);
                    }
                }
                //设置每列的宽度
                for(int i=0;i<fieldList.size();i++){
                    Field field = fieldList.get(i);
                    hssfsheet.setColumnWidth(i,field.getAnnotation(EnableExportField.class).colWidth()*20);
                }

                HSSFRow hssfRow = null;
                HSSFCell hssfcell = null;

                //绘制表头以及菜单
                String fileName =export.fileName();
                if(exportTitle!=null){
                    fileName = exportTitle;
                }

                //创建标题行（表头）
                createHeadRow(workbook,hssfRow,hssfcell,hssfsheet,colNames,colors);
                try {

                    //插入内容
                    int i=0;
                    for (Object obj : dataList) {
                        hssfRow = hssfsheet.createRow(i + 1);
                        //设置每列的宽度
                        //此处设置j=-1 ：添加一列，序号列
                        for(int j=0;j<fieldList.size();j++){

                            Field field = fieldList.get(j);
                            field.setAccessible(true);
                            Object value = field.get(obj);
                            EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                            String getMethodName = enableExportField.useGetMethod();
                            if(!"".equals( getMethodName )){
                                Method  method = clazz.getMethod(getMethodName, new Class[]{field.getType()});
                                method.setAccessible(true);
                                value= method.invoke(obj, new Object[]{value});
                            }
                            if(field.isAnnotationPresent(EnableSelectList.class)){
                                if(selectListMap!=null&& selectListMap.get(j)!=null) {
                                    value = selectListMap.get(j).get(value);
                                }
                            }
                            hssfcell = hssfRow.createCell(j);
                            hssfcell.setCellValue(String.valueOf(value));
                        }
                        i++;
                    }
                    //创建下拉列表
                    createDataValidation(hssfsheet,selectListMap);
                    workbook.write(outputStream);

                } catch (IllegalAccessException e ) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    /**
     * 导出 Excel
     * @param outputStream 输出流，用于写文件
     * @param dataList 需要导出的数据
     * @param clazz 导出数据的pojo类型
     * @param selectListMap 下拉列表的列
     * @param exportTitle 当该参数不为空则替换默认的标题
     */
    public static void exportExcel2007(OutputStream outputStream, List  dataList, Class clazz, Map<Integer,Map<String,String>> selectListMap,String exportTitle){
        //创建一个Excel工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        //建立表
        XSSFSheet hssfsheet =  workbook.createSheet();

        hssfsheet.setDefaultRowHeight( ( short )(20*20) );
        //检查当前pojo是否允许导出
        if(clazz.isAnnotationPresent(EnableExport.class)) {
            EnableExport export = (EnableExport) clazz.getAnnotation(EnableExport.class);
            //获取所有标题名称
            List<String> colNames =new ArrayList<String>();
            //获取所有标题的背景颜色
            List<ColorEnum> colors =new ArrayList<ColorEnum>();
            //所有允许导出的字段
            List<Field> fieldList = new ArrayList<Field>();
            for(Field field : clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(EnableExportField.class)){
                    EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                    colNames.add(enableExportField.colName());
                    colors.add(enableExportField.cellColor());
                    fieldList.add(field);
                }
            }
            //设置每列的宽度
            for(int i=0;i<fieldList.size();i++){
                Field field = fieldList.get(i);
                hssfsheet.setColumnWidth(i,field.getAnnotation(EnableExportField.class).colWidth()*20);
            }
            XSSFRow hssfRow = null;
            XSSFCell hssfcell = null;

            //绘制表头以及菜单
            String fileName =export.fileName();
            if(exportTitle!=null){
                fileName = exportTitle;
            }
            //创建标题行（表头）
            createHeadRow2007(workbook,hssfRow,hssfcell,hssfsheet,colNames,colors);
            try {
                //插入内容
                int i=0;
                for (Object obj : dataList) {
                    hssfRow = hssfsheet.createRow(i + 1);
                    //设置每列的宽度
                    //此处设置j=-1 ：添加一列，序号列
                    for(int j=0;j<fieldList.size();j++){

                        Field field = fieldList.get(j);
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        Object value = field.get(obj);
                        EnableExportField enableExportField = field.getAnnotation(EnableExportField.class);
                        String getMethodName = enableExportField.useGetMethod();
                        if(!"".equals( getMethodName )){
                            Method  method = clazz.getMethod(getMethodName, new Class[]{fieldType});
                            method.setAccessible(true);
                            value= method.invoke(obj, new Object[]{value});
                        }
                        if(field.isAnnotationPresent(EnableSelectList.class)){
                            if(selectListMap!=null&& selectListMap.get(j)!=null) {
                                value = selectListMap.get(j).get(value);
                            }
                        }
                        hssfcell = hssfRow.createCell(j);
                        if(fieldType.isAssignableFrom(Date.class) && value != null){
                            SimpleDateFormat sdf = new SimpleDateFormat(enableExportField.dateFormat());
                            String res = sdf.format((Date) value);
                            hssfcell.setCellValue(res);
                        }else {
                            hssfcell.setCellValue(String.valueOf(value == null ? "" : value));
                        }
                    }
                    i++;
                }
                //创建下拉列表
                createDataValidation(hssfsheet,selectListMap);
                workbook.write(outputStream);

            } catch (IllegalAccessException | IOException | NoSuchMethodException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        }
    }

        /**
         * 获取一个基本的带边框的单元格
         * @param workbook
         * @return
         */
        private static HSSFCellStyle getBasicCellStyle(HSSFWorkbook workbook,String type){
            HSSFCellStyle hssfcellstyle = workbook.createCellStyle();
            HSSFDataFormat format = workbook.createDataFormat();
            hssfcellstyle.setDataFormat(format.getFormat("@"));

            hssfcellstyle.setBorderLeft(BorderStyle.THIN);
            hssfcellstyle.setBorderBottom(BorderStyle.THIN);
            hssfcellstyle.setBorderRight(BorderStyle.THIN);
            hssfcellstyle.setBorderTop(BorderStyle.THIN);
            hssfcellstyle.setAlignment(HorizontalAlignment.CENTER);
            hssfcellstyle.setVerticalAlignment(VerticalAlignment.CENTER);
            hssfcellstyle.setWrapText(true);
            return hssfcellstyle;
        }
    /**
     * 获取一个基本的带边框的单元格
     * @param workbook
     * @return
     */
    private static XSSFCellStyle getBasicCellStyle2007(XSSFWorkbook workbook, String type){
        XSSFCellStyle hssfcellstyle = workbook.createCellStyle();
        XSSFDataFormat format = workbook.createDataFormat();
        hssfcellstyle.setDataFormat(format.getFormat("@"));

        hssfcellstyle.setBorderLeft(BorderStyle.THIN);
        hssfcellstyle.setBorderBottom(BorderStyle.THIN);
        hssfcellstyle.setBorderRight(BorderStyle.THIN);
        hssfcellstyle.setBorderTop(BorderStyle.THIN);
        hssfcellstyle.setAlignment(HorizontalAlignment.CENTER);
        hssfcellstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        hssfcellstyle.setWrapText(true);
        return hssfcellstyle;
    }
        /**
         * 获取带有背景色的标题单元格
         * @param workbook
         * @return
         */
        private static HSSFCellStyle getTitleCellStyle(HSSFWorkbook workbook, ColorEnum color){
            HSSFCellStyle hssfcellstyle =  getBasicCellStyle(workbook,null);
            hssfcellstyle.setFillForegroundColor(color.getIndex()); // 设置背景色
            hssfcellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return hssfcellstyle;
        }

    /**
     * 获取带有背景色的标题单元格
     * @param workbook
     * @return
     */
    private static XSSFCellStyle getTitleCellStyle2007(XSSFWorkbook workbook, ColorEnum color){
        XSSFCellStyle hssfcellstyle =  getBasicCellStyle2007(workbook,null);
        hssfcellstyle.setFillForegroundColor(color.getIndex()); // 设置背景色
        hssfcellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return hssfcellstyle;
    }

        /**
         * 创建一个跨列的标题行
         * @param workbook
         * @param hssfRow
         * @param hssfcell
         * @param hssfsheet
         * @param allColNum
         * @param title
         */
        private static void createTitle(HSSFWorkbook workbook, HSSFRow hssfRow , HSSFCell hssfcell, HSSFSheet hssfsheet, int allColNum, String title, ColorEnum color){
            //在sheet里增加合并单元格
            CellRangeAddress cra = new CellRangeAddress(0, 0, 0, allColNum);
            hssfsheet.addMergedRegion(cra);
            // 使用RegionUtil类为合并后的单元格添加边框
            RegionUtil.setBorderBottom(BorderStyle.THIN,cra,hssfsheet);// 下边框
            RegionUtil.setBorderLeft(BorderStyle.THIN,cra,hssfsheet); // 左边框
            RegionUtil.setBorderRight(BorderStyle.THIN,cra,hssfsheet); // 有边框
            RegionUtil.setBorderTop(BorderStyle.THIN,cra,hssfsheet); // 上边框

            //设置表头
            hssfRow = hssfsheet.getRow(0);
            hssfcell = hssfRow.getCell(0);
            hssfcell.setCellStyle( getTitleCellStyle(workbook,color));
            hssfcell.setCellType(CellType.STRING);
            hssfcell.setCellValue(title);
        }

        /**
         * 设置表头标题栏以及表格高度
         * @param workbook
         * @param hssfRow
         * @param hssfcell
         * @param hssfsheet
         * @param colNames
         */
        private static void createHeadRow(HSSFWorkbook workbook, HSSFRow hssfRow , HSSFCell hssfcell, HSSFSheet hssfsheet, List<String> colNames, List<ColorEnum> colors){
            //插入标题行,第0行
            hssfRow = hssfsheet.createRow(0);

            for (int i = 0; i < colNames.size(); i++) {
                HSSFRichTextString richString = new HSSFRichTextString(colNames.get(i));
                Font font = workbook.createFont();
                font.setColor(ColorEnum.RED.getIndex());  //颜色
                richString.applyFont(0, 1,font);
                hssfcell = hssfRow.createCell(i);
                hssfcell.setCellStyle(getTitleCellStyle(workbook,colors.get(i)));
                hssfcell.setCellType(CellType.STRING);
                if (i==0||i==1||i==4){
                    hssfcell.setCellValue(richString);
                }else {
                    hssfcell.setCellValue(colNames.get(i));
                }
            }
        }
    /**
     * 设置表头标题栏以及表格高度
     * @param workbook
     * @param hssfRow
     * @param hssfcell
     * @param hssfsheet
     * @param colNames
     */
    private static void createHeadRow2007(XSSFWorkbook workbook, XSSFRow hssfRow , XSSFCell hssfcell, XSSFSheet hssfsheet, List<String> colNames, List<ColorEnum> colors){
        //插入标题行,第0行
        hssfRow = hssfsheet.createRow(0);

        for (int i = 0; i < colNames.size(); i++) {
            XSSFRichTextString richString = new XSSFRichTextString(colNames.get(i));
//            Font font = workbook.createFont();
//            font.setColor(ColorEnum.RED.getIndex());  //颜色
//            richString.applyFont(0, 1,font);
            hssfcell = hssfRow.createCell(i);
            hssfcell.setCellStyle(getTitleCellStyle2007(workbook,colors.get(i)));
            hssfcell.setCellType(CellType.STRING);
            if (i==0||i==1||i==4){
                hssfcell.setCellValue(richString);
            }else {
                hssfcell.setCellValue(colNames.get(i));
            }
        }
    }
        /**
         * excel添加下拉数据校验
         * @param sheet 哪个 sheet 页添加校验
         * @return
         */
        public static void createDataValidation(Sheet sheet, Map<Integer,Map<String,String>> selectListMap) {
            if(selectListMap!=null) {
                for(Map.Entry<Integer,Map<String,String>> entry:selectListMap.entrySet()  ){
                    Integer key = entry.getKey();
                    Map<String,String> value = entry.getValue();
                    // 第几列校验（0开始）key 数据源数组value
                    if(value.size()>0) {
                        int i=0;
                        String[] valueArr = new String[value.size()];
                        for(Map.Entry<String,String> ent :value.entrySet()){
                            valueArr[i] = ent.getValue();
                            i++;
                        }
                        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(2, 65535, key, key);
                        DataValidationHelper helper = sheet.getDataValidationHelper();
                        DataValidationConstraint constraint = helper.createExplicitListConstraint(valueArr);
                        DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
                        //处理Excel兼容性问题
                        if (dataValidation instanceof XSSFDataValidation) {
                            dataValidation.setSuppressDropDownArrow(true);
                            dataValidation.setShowErrorBox(true);
                        } else {
                            dataValidation.setSuppressDropDownArrow(false);
                        }
                        dataValidation.setEmptyCellAllowed(true);
                        dataValidation.setShowPromptBox(true);
                        dataValidation.createPromptBox("提示", "只能选择下拉框里面的数据");
                        sheet.addValidationData(dataValidation);
                    }
                }
            }
        }
        /**
         *通过value获取key值
         * @param selectMap
         * @param value
         * @return
         */
        private static String getKeyByValue(Map<String,String> selectMap,String value){
            if(selectMap!=null){
                for(Map.Entry<String,String> ent :selectMap.entrySet()){
                    if(value!=null&&value.equals(ent.getValue())) {
                        return ent.getKey();
                    }
                }
            }else{
                return value;
            }
            return null;
        }


        /**
         *判断字符串是否为数字
         * @param str
         * @return
         */
        private static boolean isNumeric(String str) {
            if (str != null && !"".equals(str.trim())) {
                Matcher matcher = pattren.matcher(str);
                if (matcher.matches()) {
                    if (!str.contains(".") && str.startsWith("0")) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }

        /**
         *设置单元格的值
         * @param value
         * @param hssfcell
         * @param hssfRow
         * @param cellStyle
         * @param cellIndex
         */
        private static void setCellValue(Object value, HSSFCell hssfcell, HSSFRow hssfRow, CellStyle cellStyle, int cellIndex) {
            String valueStr = String.valueOf(value);
            hssfcell =hssfRow.createCell(cellIndex );
            //暂时认为数字类型不会有下拉列表
            if (isNumeric(valueStr)) {
                hssfcell.setCellStyle(cellStyle);
                hssfcell.setCellType(CellType.NUMERIC);
                hssfcell.setCellValue(Double.valueOf(valueStr));
            } else {
                hssfcell.setCellStyle(cellStyle);
                hssfcell.setCellType(CellType.STRING);
                hssfcell.setCellValue(valueStr);
            }
        }
}


