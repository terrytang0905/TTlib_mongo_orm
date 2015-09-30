package com.newroad.mongodb.orm.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.newroad.mongodb.orm.MongoSQLContext;
import com.newroad.mongodb.orm.mapping.Mapping;
import com.newroad.mongodb.orm.mapping.MappingEntry;
import com.newroad.mongodb.orm.parser.NodeParser;
import com.newroad.mongodb.orm.parser.ParserCallBack;
import com.newroad.mongodb.orm.parser.ParserWapper;

/**
 * @info  : 用于解析 select result 节点  查询后  的分析器 
 * @author: tangzj
 * @data  : 2013-6-7
 * @since : 1.5
 */
@SuppressWarnings("unchecked")
public class SelectResultNodeAfterParser implements NodeParser<Object> {
	
	@Override
	public Object parser (Mapping orm, MongoSQLContext context, Object obj) throws Exception {
		if (orm==null) {
			return obj;
		}
		
		if (!StringUtils.isBlank(orm.getId())) {
			Mapping map = context.getOM(orm.getId());   // 全局mapping映射
			List<MappingEntry> om = map.getOm();
			Class<?> clazz = map.getClazz();
			if (obj instanceof List) {
				List<JSONObject> list = (List<JSONObject>)obj;
				List<Object> result = new ArrayList<Object>(list.size());   // 返回列表
				for (JSONObject json : list) {
					if (clazz.equals(Map.class)) {   // Map 转换
						result.add(ParserWapper.jsonToMap(context, om, json, callBack));
					} else {  //  对象转换
						result.add(ParserWapper.jsonToObject(context, om, clazz, json, callBack));
					}
				}
				return result;
			} else {
				if (clazz.equals(Map.class)) {   // Map 转换
					return ParserWapper.jsonToMap(context, om, obj, callBack);
				} else {  //  对象转换
					return ParserWapper.jsonToObject(context, om, clazz, obj, callBack);
				}
			}
		} else if (orm.getClazz() !=null){
			Class<?> clazz = orm.getClazz();
			List<MappingEntry> om = orm.getOm();
			if (obj instanceof List) {
				List<JSONObject> list = (List<JSONObject>)obj;
				List<Object> result = new ArrayList<Object>(list.size());   // 返回列表
				for (JSONObject json : list) {
					if (clazz.equals(Map.class)) {   // Map 转换
						result.add(ParserWapper.jsonToMap(context, om, json, callBack));
					} else {  
						if (ParserWapper.unitClass.contains(clazz)) {
							for(MappingEntry entry : om) {
								result.add(ParserWapper.jsonToUnit(clazz, json, entry.getColumn()));
							}
						} else { //  对象转换
							result.add(ParserWapper.jsonToObject(context, om, clazz, json, callBack));
						}
					}
				}
				return result;
			} else {
				if (clazz.equals(Map.class)) {   // Map 转换
					return ParserWapper.jsonToMap(context, om, obj, callBack);
				} else {  //  对象转换
					Object result = null;
					if (ParserWapper.unitClass.contains(clazz)) {
						for(MappingEntry entry : om) {
							result = ParserWapper.jsonToUnit(clazz,  (JSONObject)obj, entry.getColumn());
						}
					} else {
						result = ParserWapper.jsonToObject(context, om, clazz, obj, callBack);
					}
					return result;
				}
			}
		} else {
			throw new RuntimeException("SQL配置规则异常, 请设置mapping或class属性");
		}
	}
	
	/**
	 *  递归时回调
	 */
	ParserCallBack<Object> callBack = new ParserCallBack<Object>() {
		@Override
		public Object callBack(Mapping orm, MongoSQLContext context, Object obj) throws Exception {
			return parser(orm, context, obj);
		}
	};
}
