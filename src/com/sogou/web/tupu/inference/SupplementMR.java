package com.sogou.web.tupu.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

public class SupplementMR extends MyMR {

	@Override
	public void myInferenceReduce(MyKeyValue inKv, MyKeyValue outKv,
			TaskInputOutputContext context) throws IOException,
			InterruptedException {
		boolean needSupplement = false, hasVideoLink = false, isVideo=false;
		String name = null;
		Map<String, String> supplementProp = new HashMap<String, String>();
		for(int i = 0 ; i<inKv.values.size(); i++){
			String val = inKv.values.get(i);
			if(val == null)continue;
			String [] tks = val.split("\t");
			if(tks.length==6){
				if( !tks[3].startsWith("����_ԭ��") 
						&& !tks[3].startsWith("���������")
						&& !tks[3].startsWith("��ѯ�ȶ�1")
						&& !tks[3].startsWith("TYPE��Ҫ��")
						&& !tks[3].startsWith("PAGEID")){
					needSupplement = true;
					name = tks[2];
				}else{
					supplementProp.put(tks[3], tks[4]);
					//delete original data from supplement dir
					inKv.values.set(i, null);
				}
				if(tks[3].startsWith("��Ӱ_")){
					isVideo = true;
				}
				if(tks[3].startsWith("��Ӱ_�ѹ���Ƶ����")){
					hasVideoLink = true;
				}
				/*
				if(tks[3].startsWith("��Ӱ_")||tks[3].startsWith("���Ӿ�_")){
					isVideo = true;
				}
				if(tks[3].startsWith("��Ӱ_�ѹ���Ƶ����")||tks[3].startsWith("���Ӿ�_�ѹ���Ƶ����")){
					hasVideoLink = true;
				}
				*/
			}
		}
		if(!needSupplement){
			inKv.values.clear();
		}else{
			Iterator it = supplementProp.keySet().iterator();
			while(it.hasNext()){
				String key = (String) it.next();
				String value = supplementProp.get(key);
				if(isVideo && !hasVideoLink && key.equals("TYPE��Ҫ��")){
					continue;
				}
				//MyMR.reduceOutput(inKv.key, "SOGOUID_"+inKv.key+"\t"+name+"\t"+key + "\t" + value+"\t-1", context);
				inKv.values.add(inKv.key+"\tSOGOUID_"+inKv.key+"\t"+name+"\t"+key + "\t" + value+"\t-1");
			}
		}
	}
}
