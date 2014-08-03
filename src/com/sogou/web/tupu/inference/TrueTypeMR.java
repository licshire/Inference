package com.sogou.web.tupu.inference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.hsqldb.lib.Iterator;

public class TrueTypeMR extends MyMR {

	@Override
	public void myInferenceReduce(MyKeyValue inKv, MyKeyValue outKv,
			TaskInputOutputContext context) throws IOException,
			InterruptedException {
		boolean isCartoon = false;
		boolean isTv = false;
		String name = null, url = null;
		for(int i = 0 ; i<inKv.values.size(); i++){
			String val = inKv.values.get(i);
			if(val==null)continue;
			String[] tks = val.split("\t");
			if(tks.length==6){
				url = tks[1];
				name = tks[2];
				if(tks[3].contains("���Ӿ�_")){
					isTv = true;
				}
				if(tks[3].equals("���Ӿ�_����")){
					if(tks[4].contains("����") 
							|| tks[4].contains("��ͯ")){
						isCartoon = true;
					}
				}
			}
		}
		if(isTv && !isCartoon && name != null && url!= null){
			MyMR.reduceOutput(inKv.key, url + "\t"+name+"\tTRUETYPE\t���Ӿ�\t-1", context);
		}
	}

}
