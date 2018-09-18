package io.daex.sdk.core.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qingyun.yu on 2018/6/26.
 */
public class ObjectUtil {
    public static <T> T convertObject(Object objA, Class<T> clazz, Map<String, String> propertyMap, Map<String, String> valueMap, List<String> excludeList) {
        T t = null;
        try {
            Map<String, Field> objAMap = getAllField(objA.getClass());
            t = clazz.newInstance();
            convertObject(objA, objAMap, t, propertyMap, valueMap, excludeList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public static <T> T convertObject(Object fromObj, Map<String, Field> fieldMap, T t, Map<String, String> propertyMap, Map<String, String> valueMap, List<String> excludeList) throws IllegalAccessException {
        for (Field field : t.getClass().getDeclaredFields()) {
            if (!excludeList.contains(field)) {
                DataType type = getType(field);
                if (!DataType.OBJECT.equals(type)) {
                    if (valueMap != null && valueMap.containsKey(field.getName())) {
                        field.setAccessible(true);
                        Object value = valueMap.get(field.getName());
                        value = convertDataType(type, value);
                        if (value != null) {
                            field.set(t, value);
                        }
                    } else if (propertyMap != null && propertyMap.containsKey(field.getName())) {
                        String fieldName = propertyMap.get(field.getName());
                        Field fromField = fieldMap.get(fieldName);
                        setValue(fromObj, fromField, field, t, type);
                    } else if (fieldMap.containsKey(field.getName())) {
                        Field fromField = fieldMap.get(field.getName());
                        setValue(fromObj, fromField, field, t, type);
                    }
                }
            }
        }
        return t;
    }

    public static<T> void setValue(Object fromObj, Field fromField,Field toField, T t, DataType type) throws IllegalAccessException {
        DataType fromType = getType(fromField);
        toField.setAccessible(true);
        fromField.setAccessible(true);
        Object value = fromField.get(fromObj);
        if (type.equals(fromType)) {
            if (value != null) {
                toField.set(t, value);
            }
        } else {
            value = convertDataType(type, value);
            if (value != null) {
                toField.set(t, value);
            }
        }
    }

    public static Object convertDataType(DataType type, Object value) {
        if (value == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return value.toString();
            case SHORT:
                return Short.decode(value.toString());
            case FLOAT:
                return Float.valueOf(value.toString());
            case INT:
                return Integer.valueOf(value.toString());
            case BYTE:
                return Byte.decode(value.toString());
            case BOOLEAN:
                return Boolean.valueOf(value.toString());
            case LONG:
                return Long.valueOf(value.toString());
            case DOUBLE:
                return Double.valueOf(value.toString());
            case CHAR:
                return value.toString().toCharArray()[0];
            case BIGDECIMAL:
                return BigDecimal.valueOf(Long.valueOf(value.toString()));
            case DATE:
                return Date.valueOf(value.toString());
            default:
                return null;
        }
    }

    public static Map<String, Field> getAllField(Class clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        if ("Object".equals(clazz.getSimpleName())) {
            return fieldMap;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        fieldMap.putAll(getAllField(clazz.getSuperclass()));
        return fieldMap;
    }

    private static DataType getType(Field field) {
        switch (field.getType().getSimpleName()) {
            case "String":
                return DataType.STRING;
            case "Short":
            case "short":
                return DataType.SHORT;
            case "Float":
            case "float":
                return DataType.FLOAT;
            case "Integer":
            case "int":
                return DataType.INT;
            case "Byte":
            case "byte":
                return DataType.BYTE;
            case "Boolean":
            case "boolean":
                return DataType.BOOLEAN;
            case "Long":
            case "long":
                return DataType.LONG;
            case "Double":
            case "double":
                return DataType.DOUBLE;
            case "Character":
            case "char":
                return DataType.CHAR;
            case "BigDecimal":
                return DataType.BIGDECIMAL;
            case "Date":
                return DataType.DATE;
            default:
                return DataType.OBJECT;
        }
    }

    enum DataType {
        BYTE, SHORT, CHAR, INT, FLOAT, LONG, DOUBLE, BOOLEAN, STRING, BIGDECIMAL, DATE, OBJECT
    }
}


