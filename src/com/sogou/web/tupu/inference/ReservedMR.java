package com.sogou.web.tupu.inference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

public class ReservedMR extends MyMR {
	
	@Override
	public void myInferenceReduce(MyKeyValue inKv, MyKeyValue outKv,
			TaskInputOutputContext context) throws IOException,
			InterruptedException {
		String newpv = null,newimportance = null, name = null, url = null, newscore= null, newtype=null; 
		for(int i = 0 ; i < inKv.values.size(); i++){
			String val = inKv.values.get(i);
			String[] tks = val.split("\t");
			if(name== null && tks.length==6 && tks[3].equals("����")){
				name = tks[2];
			}
			if(url== null && tks.length==6 && !tks[1].equals("ADD") && tks[1].contains("http:")){
				url = tks[1];
			}
			if(tks.length>3 && tks[1].equals("ADD")){
				if(tks[2].equals("��ѯ�ȶ�")){
					newpv = tks[3];
				}
				if(tks[2].equals("��Ҫ��")){
					newimportance = tks[3];
				}
				if(tks[2].equals("��Ϸ_����")){
					newscore = tks[3];
				}
				if(tks[2].equals("��Ϸ_��Ϸ����")){
					newtype = tks[3];
				}
				inKv.values.set(i, null);
			}
		}
		for(int i = 0 ; i < inKv.values.size(); i++){
			String val = inKv.values.get(i);
			if(val==null)continue;
			String[] tks = val.split("\t");
			if(tks.length==6 
					&& tks[3].equals("��ѯ�ȶ�")
					&& newpv!=null){
				inKv.values.set(i, null);
			}
			if(tks.length==6 
					&& tks[3].equals("��Ҫ��")
					&& newpv!=null){
				inKv.values.set(i, null);
			}
			if(tks.length==6 
					&& tks[3].equals("��Ϸ_����")
					&& newscore!=null){
				inKv.values.set(i, null);
			}
			if(tks.length==6 
					&& tks[3].equals("��Ϸ_��Ϸ����")
					&& newtype!=null){
				inKv.values.set(i, null);
			}
		}
		if(newpv!=null){
			MyMR.reduceOutput(inKv.key, url+"\t"+name+"\t��ѯ�ȶ�\t"+newpv+"\t-1", context);
		}
		if(newimportance!=null){
			MyMR.reduceOutput(inKv.key, url+"\t"+name+"\t��Ҫ��\t"+newimportance+"\t-1", context);
		}
		if(newscore!=null){
			MyMR.reduceOutput(inKv.key, url+"\t"+name+"\t��Ϸ_����\t"+newscore+"\t-1", context);
		}
		if(newtype!=null){
			MyMR.reduceOutput(inKv.key, url+"\t"+name+"\t��Ϸ_��Ϸ����\t"+newtype+"\t-1", context);
		}
	}


}
