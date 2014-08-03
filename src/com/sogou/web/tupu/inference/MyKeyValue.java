package com.sogou.web.tupu.inference;

import java.util.ArrayList;
import java.util.List;

public class MyKeyValue {
	
	public String key = null; //ʵ���id
	public List<String> values = null; //һ��ʵ�����е�6Ԫ�飬��merge������ɵ���������
	
	public MyKeyValue(){
		key = null;
		values = new ArrayList<String>();
	}
	public MyKeyValue(String _key, String _value){
		values = new ArrayList<String>();
		if(_key!=null)
			this.key = _key;
		if(_value!=null)
			this.values.add(_value);
	}
	
	public void addValues(List<String> vals){
		for(String value: vals){
			values.add(value);
		}
	}
	
}
