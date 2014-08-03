package com.sogou.web.tupu.inference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

public class PeiyinMR extends MyMR {
	public Map<String, String> mTag = null;
	@Override
	public void myInferenceReduceSetup(Configuration conf) throws IOException {
		String filePath = MyMR.getConfigureValue(conf,"fs.path.inference.home")+"/Inference/conf/tag.conf";
		Path configPath = new Path(filePath);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(configPath) && fs.isFile(configPath)) {
			mTag = new HashMap<String, String>();
			FSDataInputStream in = fs.open(configPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"GBK"));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				String[] seg = line.split("\t");
				if (seg.length != 1)
					continue;
				mTag.put(seg[0], "");
			}
			br.close();
		}else{
			System.err.printf("load file %s err!", filePath);
			System.exit(1);
		}
	}
	
	private void addTagLine(String id, String url, String name, String tag, TaskInputOutputContext context ) throws IOException, InterruptedException{
		try {
			String md5str = InferenceMR.getMd5String(url+tag);
//MyMR.reduceOutput(null,"MD5:" + url+tag + ", " + md5str, context);
			MyMR.reduceOutput(id, url+"\t"+name+"\t��ǩ:��ǩ_��ǩ��@" +md5str+"\t"+ tag + "\t-1", context);
			MyMR.reduceOutput(id, url+"\t"+name+"\t��ǩ:��ǩ_Ȩ��@" +md5str+"\t100\t-1", context);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void myInferenceReduce(MyKeyValue inKv, MyKeyValue outKv,
			TaskInputOutputContext context) throws IOException, InterruptedException {
		List<String> properties = inKv.values;
		Set<String> addedSet = new HashSet<String>();
		Set<String> originalSet = new HashSet<String>();
		boolean isCartoon = false;
		boolean isCartoonTV = false;
		boolean isGroup = false;
		boolean isMainLand = false;
		boolean hasWar = false;
		boolean isOld = false;
		boolean isJapan = false;
		boolean isHorrible = false;
		boolean isReligion = false;
		String url = null, name = null;
		for (String property_line : properties) {
			String[] tks = property_line.split("\t");
			if (tks.length == 6) {
				if(!isJapan){
					if(tks[3].equals("���") || tks[3].contains("����")){
						if(tks[4].contains("����")
								||tks[4].contains("�ձ�")
								||tks[4].contains("����")){
							isJapan = true;
						}
					}
				}
				if (tks[3].equals("��Ӱ_��Ӱ����")) {
					if (tks[4].equals("����") || tks[4].equals("����")) {
						isCartoon = true;
					}
					if(mTag.containsKey(tks[4])){
						addedSet.add(tks[4]);
					}
					if(tks[4].contains("ս��")){
						hasWar = true;
					}
					if(tks[4].contains("��װ")
							||tks[4].contains("�Ŵ�")){
						isOld = true;
					}
					if(url == null){
						url = tks[1];
					}
					if(name == null){
						name = tks[2];
					}
				} else if (tks[3].equals("���Ӿ�_����")) {
					if (tks[4].equals("����") || tks[4].equals("����")) {
						isCartoonTV = true;
					}
					if(mTag.containsKey(tks[4])){
						addedSet.add(tks[4]);
					}
					if(tks[4].contains("ս��")){
						hasWar = true;
					}
					if(tks[4].contains("��װ")
							||tks[4].contains("�Ŵ�")){
						isOld = true;
					}
					if(url == null){
						url = tks[1];
					}
					if(name == null){
						name = tks[2];
					}
				}
				
				if(tks[3].equals("��Ӱ_��Ƭ����") || tks[3].equals("���Ӿ�_��Ƭ����")){
					if(url == null){
						url = tks[1];
					}
					if(name == null){
						name = tks[2];
					}
					if(tks[4].equals("�й���½")){
						isMainLand = true;
					}
				}
				
				if(tks[3].startsWith("��ǩ:��ǩ_��ǩ��")){
					if(tks[4].equals("�ֲ�")||tks[4].equals("���")){
						isHorrible = true;
					}
					if(tks[4].startsWith("�ڽ�")){
						isReligion = true;
					}
					originalSet.add(tks[4]);
				}
				if(tks[1].startsWith("Sogoucomp_")){
					isGroup = true;
				}
			}
		}
		Iterator it = (Iterator) addedSet.iterator();
		while(it.hasNext()){
			String tag = (String) it.next();
			if(!originalSet.contains(tag))
				addTagLine(inKv.key, url , name, tag, context );
		}
		if(url != null && name != null ){
			if(hasWar && isMainLand  && !isOld && isJapan && !addedSet.contains("����") && !originalSet.contains("����")){
				addTagLine(inKv.key, url, name, "����", context);
				addedSet.add("����");
			}
			if(isHorrible && name.startsWith("��") && !name.contains(":") && !addedSet.contains("��")&& !originalSet.contains("��")){
				addTagLine(inKv.key, url, name, "��", context);
				addedSet.add("��");
			}
			if(isReligion && (name.contains("��") || name.contains("��")) && !addedSet.contains("���")&& !originalSet.contains("���")){
				addTagLine(inKv.key, url, name, "���", context);
				addedSet.add("���");
			}
			if(name.contains("������")&& !addedSet.contains("������")&& !originalSet.contains("������")){
				addTagLine(inKv.key, url, name, "������", context);
				addedSet.add("������");
			}
			if(name.contains("�߿�")&& !addedSet.contains("�߿�")&& !originalSet.contains("�߿�")){
				addTagLine(inKv.key, url, name, "�߿�", context);
				addedSet.add("�߿�");
			}
			if(name.contains("����")&& !addedSet.contains("����")&& !originalSet.contains("����")){
				addTagLine(inKv.key, url, name, "����", context);
				addedSet.add("����");
			}
			if( ( name.contains("��") || name.contains("��") ) && isHorrible && !addedSet.contains("����")&& !originalSet.contains("����")){
				addTagLine(inKv.key, url, name, "����", context);
				addedSet.add("����");
			}
		}
		
		for (int i = 0; i < properties.size(); i++) {
			String property_line = properties.get(i);
			String[] tks = property_line.split("\t");
			if (tks.length == 6) {
				if (isCartoon) {
					if (tks[3].contains("��Ӱ_��Ա��:��Ӱ��Ա������_��Ա")) {
						property_line = property_line.replace(
								"��Ӱ_��Ա��:��Ӱ��Ա������_��Ա", "��Ӱ_��Ա��:��Ӱ��������_��Ա");
					}
					if (tks[3].equals("��Ӱ_����")) {
						property_line = property_line.replace("��Ӱ_����",
								"��Ӱ_������Ա");
					}
				}
				if (isCartoonTV) {
					if (tks[3].contains("���Ӿ�_��Ա��:���Ӿ���Ա������_��Ա")) {
						property_line = property_line.replace(
								"���Ӿ�_��Ա��:���Ӿ���Ա������_��Ա", "���Ӿ�_��Ա��:���Ӿ���������_��Ա");
					}
					if (tks[3].equals("���Ӿ�_����")) {
						property_line = property_line.replace("���Ӿ�_����",
								"���Ӿ�_������Ա");
					}
				}
			}
			if (isGroup) {
				property_line = property_line.replace("��Ӱ_��Ա��:��Ӱ��Ա������_��Ա",
						"��Ӱϵ��_ϵ����Ա��:ϵ�е�Ӱ��Ա��_��Ա");
				property_line = property_line.replace("��Ӱ_��Ա��:��Ӱ��Ա������_��ɫ",
						"��Ӱϵ��_ϵ����Ա��:ϵ�е�Ӱ��Ա��_��ɫ");
				property_line = property_line.replace("��Ӱ_��Ա��:��Ӱ��Ա������_Ȩ��",
						"��Ӱϵ��_ϵ����Ա��:ϵ�е�Ӱ��Ա��_Ȩ��");

				property_line = property_line.replace("���Ӿ�_��Ա��:���Ӿ���Ա������_��Ա",
						"���Ӿ�ϵ��_ϵ����Ա��:���Ӿ�ϵ����Ա��_��Ա");
				property_line = property_line.replace("���Ӿ�_��Ա��:���Ӿ���Ա������_��ɫ",
						"���Ӿ�ϵ��_ϵ����Ա��:���Ӿ�ϵ����Ա��_��ɫ");
				property_line = property_line.replace("���Ӿ�_��Ա��:���Ӿ���Ա������_Ȩ��",
						"���Ӿ�ϵ��_ϵ����Ա��:���Ӿ�ϵ����Ա��_Ȩ��");
			}
			properties.set(i, property_line);
		}
		return;
	}

}
