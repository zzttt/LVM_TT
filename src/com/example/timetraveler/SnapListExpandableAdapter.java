package com.example.timetraveler;

import java.util.ArrayList;

import com.example.timetraveler.BaseExpandableAdapter.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SnapListExpandableAdapter extends BaseExpandableListAdapter {


	private ArrayList<String> groupList = null;
	private ArrayList<ArrayList<String>> childList = null;
	private ArrayList<String> destList = null;
	private ArrayList<ArrayList<String>> mchildDestList = null;
	private LayoutInflater inflater = null;
	private ViewHolder viewHolder = null;
	private int menuNumber;

	public SnapListExpandableAdapter(Context c, ArrayList<String> groupList,
			ArrayList<ArrayList<String>> childList, ArrayList<String> destList, ArrayList<ArrayList<String>> mchildDestList
			,int menuNumber) {
		super();

		this.inflater = LayoutInflater.from(c);
		this.groupList = groupList;
		this.childList = childList;
		this.destList = destList;
		this.mchildDestList = mchildDestList;
		this.menuNumber = menuNumber;
	}

	
	public String getDesc(int groupPosition) {
		return destList.get(groupPosition);
	}
	
	public String getChildDesc(int groupPosition,int childPosition) {
		if(menuNumber == 0){
			switch(groupPosition){
			case 0:
				return mchildDestList.get(groupPosition).get(childPosition);
			case 1:
				return "����� �����մϴ�.";
			case 2:
				return "�ڵ� ������ ��뿩�θ� �����մϴ�.";
			}
		}else{
			//Log.i("eee", Integer.toString(groupPosition)+","+Integer.toString(childPosition));
			return mchildDestList.get(groupPosition).get(childPosition);
		}
		return null;
	}

	// �׷� �������� ��ȯ�Ѵ�.
	@Override
	public String getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	// �׷� ����� ��ȯ�Ѵ�.
	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	// �׷� ID�� ��ȯ�Ѵ�.
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	
	// �׷�� ������ ROW
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) { // �޴� 1 ( Back Up )
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.list_row, parent, false);
			// viewHolder.tv_groupName : groupName �� TextView
			// v.setLayoutParams(new
			// LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));

			viewHolder.tv_groupName = (TextView) v.findViewById(R.id.tv_group);
			viewHolder.tv_childName = (TextView) v.findViewById(R.id.tv_child);
			viewHolder.iv_image = (ImageView) v.findViewById(R.id.iv_image);
			viewHolder.tv_description = (TextView) v.findViewById(R.id.tv_desc);

			viewHolder.iv_image.setVisibility(View.GONE);
			// Child View Text�� margin - left ����
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(20, 0, 0, 0);
			viewHolder.tv_groupName.setLayoutParams(lp);
			viewHolder.tv_groupName
					.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));

			// Group name Text Size set
			viewHolder.tv_groupName.setTextSize(30);
			viewHolder.tv_groupName.setPadding(0, 20, 0, 20);
			viewHolder.tv_groupName.setTypeface(null, Typeface.BOLD);

			if (menuNumber == 0) { // 0 �� �޴������� ����ϴ� View
			} else {
				viewHolder.tv_groupName.setGravity(Gravity.CENTER_HORIZONTAL);
				v .setBackgroundColor(Color.BLACK);
				viewHolder.tv_description.setVisibility(View.GONE);
				viewHolder.iv_image.setVisibility(View.GONE);
			}

			viewHolder.tv_childName.setVisibility(View.GONE);
			v.setTag(viewHolder);

		} else {

			viewHolder = (ViewHolder) v.getTag();

		}

		// �׷��� ��ĥ���� ������ �������� ������ �ش�.
		if (isExpanded) {
			viewHolder.iv_image.setBackgroundColor(Color.GREEN);
		} else {
			viewHolder.iv_image.setBackgroundColor(Color.WHITE);
		}

		viewHolder.tv_groupName.setText(getGroup(groupPosition));

		return v;
	}

	// ���ϵ�並 ��ȯ�Ѵ�.
	@Override
	public String getChild(int groupPosition, int childPosition) {
		return childList.get(groupPosition).get(childPosition);
	}


	public void setChild(int groupPosition,String charSet) {
		childList.get(groupPosition).add(charSet);
	}
	
	public void setChildDesc(int groupPosition ,String charSet) {
		childList.get(groupPosition).add(charSet);
	}
	
	// ���ϵ�� ����� ��ȯ�Ѵ�.
	@Override
	public int getChildrenCount(int groupPosition) {
		return childList.get(groupPosition).size();
	}

	// ���ϵ�� ID�� ��ȯ�Ѵ�.
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// ���ϵ�� ������ ROW
	// child View ������ ChildName �� description �� ���
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.list_row, null);

			viewHolder.tv_childName = (TextView) v.findViewById(R.id.tv_child);
			viewHolder.tv_groupName = (TextView) v.findViewById(R.id.tv_group);
			viewHolder.tv_description = (TextView) v.findViewById(R.id.tv_desc);

			// Child View Text�� margin - left ����
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(20, 0, 0, 0);
			viewHolder.tv_childName.setLayoutParams(lp);
			viewHolder.tv_childName
					.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));

			viewHolder.tv_description.setPadding(0, 5, 0, 15);
			viewHolder.tv_description.setTextSize(18);
			viewHolder.tv_description.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));

			viewHolder.tv_groupName.setVisibility(View.GONE);
			v .setBackgroundColor(Color.DKGRAY);
			v.setTag(viewHolder); 
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		viewHolder.tv_childName.setText(getChild(groupPosition, childPosition));
		String desc = getChildDesc(groupPosition, childPosition);
		if(desc.equals("s") || desc.equals("d")){
			viewHolder.tv_description.setVisibility(View.GONE);
			viewHolder.tv_childName.setPadding(0, 25, 0, 25);
		}else{
			viewHolder.tv_description
			.setText(desc);
		}
		
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class ViewHolder {
		public ImageView iv_image;
		public TextView tv_groupName;
		public TextView tv_description;
		public TextView tv_childName;
	}

}