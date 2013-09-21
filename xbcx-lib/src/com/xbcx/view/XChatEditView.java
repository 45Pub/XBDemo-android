package com.xbcx.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xbcx.core.RecordViewHelper;
import com.xbcx.im.ExpressionCoding;
import com.xbcx.im.ui.EditViewExpressionProvider;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.SendPlugin;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class XChatEditView extends BaseEditView implements View.OnClickListener,
															View.OnFocusChangeListener,
															RecordViewHelper.OnRecordListener{

	protected static final int EXPRESSION_ONEPAGE_COUNT = 23;
	
	protected View 				mBtnPressTalk;
	protected View				mViewInput;
	
	protected View				mBtnExpression;
	protected View				mViewExpressionSet;
	protected ViewGroup			mViewExpressionTab;
	protected ViewGroup			mViewExpressionContent;
	protected View				mViewSwitch;
	
	protected View				mViewMoreSet;
	
	protected boolean			mIsInitRecordView;
	protected RecordViewHelper 	mRecordViewHelper;
	
	protected OnEditListener 	mOnEditListner;
	
	protected SparseIntArray	mMapSendPluginViewIds = new SparseIntArray();
	
	protected HashMap<View, ExpressionTab> 	mMapTabToExpressionTab = new HashMap<View,ExpressionTab>();
	protected ExpressionTab					mLastExpressionTab;
	
	public XChatEditView(Context context) {
		super(context);
		init();
	}
	
	public XChatEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		View v = LayoutInflater.from(getContext()).inflate(R.layout.chatedit, null);
		
		mBtnPressTalk = v.findViewById(R.id.btnPressTalk);
		mEditText = (EditText)v.findViewById(R.id.etTalk);
		mViewInput = v.findViewById(R.id.viewInput);
		int expressionSetId = getResources().getIdentifier(
				"viewExpressionSet", "id", getContext().getPackageName());
		if(expressionSetId != 0){
			mViewExpressionSet = v.findViewById(expressionSetId);
			if(mViewExpressionSet != null){
				mBtnExpression = v.findViewById(R.id.btnExpression);
				mBtnExpression.setOnClickListener(this);
			}
		}
		mViewMoreSet = v.findViewById(R.id.viewMoreSet);
		
		mViewSwitch = v.findViewById(R.id.btnSwitch);
		mViewSwitch.setOnClickListener(this);
		v.findViewById(R.id.btnAdd).setOnClickListener(this);
		v.findViewById(R.id.btnSend).setOnClickListener(this);
		mBtnPressTalk.setOnClickListener(this);
		
		mEditText.setOnFocusChangeListener(this);
		mEditText.setOnClickListener(this);
		mEditText.setFocusableInTouchMode(false);
		SystemUtils.addEditTextLengthFilter(mEditText, 500);
		
		mInputMethodManager = (InputMethodManager)getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		
		addView(v);
		
		initExpressionView();
		
		initMoreSetView();
	}
	
	private void initRecordPrompt(){
		if(!mIsInitRecordView){
			mRecordViewHelper = onCreateRecordViewHelper();
			mRecordViewHelper.onCreate(mBtnPressTalk);
			mRecordViewHelper.setOnRecordListener(this);
			mIsInitRecordView = true;
		}
	}
	
	private void initExpressionView(){
		if(mViewExpressionSet != null){
			addPullUpView(mViewExpressionSet);
			
			int viewId = getResources().getIdentifier(
					"viewExpressionTab", "id", getContext().getPackageName());
			if(viewId != 0){
				mViewExpressionTab = (ViewGroup)mViewExpressionSet.findViewById(viewId);
			}
			viewId = getResources().getIdentifier("viewExpressionContent", "id", getContext().getPackageName());
			if(viewId != 0){
				mViewExpressionContent = (ViewGroup)mViewExpressionSet.findViewById(viewId);
			}
			
			final List<Class<? extends EditViewExpressionProvider>> classes = IMGlobalSetting.editViewExpProviders;
			List<EditViewExpressionProvider> providers = new ArrayList<EditViewExpressionProvider>();
			for(Class<? extends EditViewExpressionProvider> c : classes){
				try{
					providers.add(c.newInstance());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(mViewExpressionTab == null){
				if(providers.size() > 0){
					EditViewExpressionProvider provider = providers.get(0);
					final View content = provider.createTabContent(getContext());
					if(content != null){
						if(mViewExpressionContent != null){
							mViewExpressionContent.addView(content, 
									new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
											ViewGroup.LayoutParams.MATCH_PARENT));
							provider.onAttachToEditView(this);
						}
					}
				}
			}else{
				boolean bAddTab = true;
				if(IMGlobalSetting.editViewExpProviders.size() == 1){
					mViewExpressionTab.setVisibility(View.GONE);
					bAddTab = false;
				}

				for(EditViewExpressionProvider provider : providers){
					ExpressionTab tab = new ExpressionTab();
					tab.mIsTabSelectable = provider.isTabSeletable();
					tab.mTabButton = provider.createTabButton(getContext());
					tab.mTabContent = provider.createTabContent(getContext());
					if(tab.mTabButton != null && bAddTab){
						tab.mTabButton.setOnClickListener(this);
						mViewExpressionTab.addView(tab.mTabButton);
						mMapTabToExpressionTab.put(tab.mTabButton, tab);
					}else{
						if(!bAddTab){
							if(mViewExpressionContent != null){
								mViewExpressionContent.addView(tab.mTabContent, 
										new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
												ViewGroup.LayoutParams.MATCH_PARENT));
							}
						}
					}
					provider.onAttachToEditView(this);
				}
				if(bAddTab){
					setExpressionCurrentTab(0);
				}
			}
			
			hidePullUpView(mViewExpressionSet, false);
		}
	}
	
	private void initMoreSetView(){
		addPullUpView(mViewMoreSet);
		hidePullUpView(mViewMoreSet, false);
	}
	
	protected int[] getExpressionResIds(){
		return ExpressionCoding.getExpressionResIds();
	}
	
	public void addViewIdSendPlugin(int viewId){
		mMapSendPluginViewIds.put(viewId, viewId);
		findViewById(viewId).setOnClickListener(this);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		initRecordPrompt();
	}
	
	protected RecordViewHelper onCreateRecordViewHelper(){
		return new RecordViewHelper();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		mRecordViewHelper.onDestroy();
	}
	
	public void	setExpressionCurrentTab(int index){
		if(mViewExpressionTab != null){
			View v = mViewExpressionTab.getChildAt(index);
			setExpressionCurrentTab(v);
		}
	}
	
	public void setExpressionCurrentTab(View v){
		if(v != null){
			setExpressionCurrentTabInternal(mMapTabToExpressionTab.get(v));
		}
	}
	
	public void setExpressionCurrentTabInternal(ExpressionTab tab){
		if(tab != null){
			if(mLastExpressionTab != null){
				mLastExpressionTab.mTabButton.setSelected(false);
				mLastExpressionTab.mTabContent.setVisibility(View.GONE);
			}
			tab.mTabButton.setSelected(true);
			if(tab.mTabContent.getParent() == null){
				mViewExpressionContent.addView(tab.mTabContent, 
						new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT));
			}
			tab.mTabContent.setVisibility(View.VISIBLE);
			mLastExpressionTab = tab;
		}
	}
	
	public EditText			getEditText(){
		return mEditText;
	}
	
	public OnEditListener	getOnEditListener(){
		return mOnEditListner;
	}

	@Override
	public void onClick(View v) {
		final int nId = v.getId();
		if(v == mBtnExpression){
			if(isPullUpViewVisible(mViewExpressionSet)){
				hidePullUpView(mViewExpressionSet, true);
			}else{
				showPullUpview(mViewExpressionSet);
				mViewSwitch.setBackgroundResource(R.drawable.msg_bar_voice);
				mViewInput.setVisibility(View.VISIBLE);
				mBtnPressTalk.setVisibility(View.GONE);
				mEditText.setFocusableInTouchMode(true);
				mEditText.requestFocus();
			}
		}else if(nId == R.id.btnSwitch){
			onClickSwitchBtn(v);
		}else if(nId == R.id.etTalk){
			showInputMethod();
		}else if(nId == R.id.btnSend){
			String strMessage = mEditText.getText().toString();
			sendText(strMessage);
		}else if(nId == R.id.btnAdd){
			if(isPullUpViewVisible(mViewMoreSet)){
				hidePullUpView(mViewMoreSet, true);
			}else{
				showPullUpview(mViewMoreSet);
			}
		}else{
			if(mMapSendPluginViewIds.get(nId,-1) != -1){
				if(mOnEditListner != null){
					SendPlugin sp = IMGlobalSetting.mapEditViewBtnIdToSendPlugin.get(nId);
					mOnEditListner.onSendPlugin(sp);
				}
			}else{
				setExpressionCurrentTabInternal(mMapTabToExpressionTab.get(v));
			}
		}
	}
	
	public void sendText(String text){
		text = text.trim();
		if (!TextUtils.isEmpty(text)) {
			if(mOnEditListner != null){
				if(mOnEditListner.onSendCheck()){
					mOnEditListner.onSendText(text);
				
					mEditText.getEditableText().clear();
				}
			}
		}
	}
	
	protected void onClickSwitchBtn(View v){
		if(mViewInput.getVisibility() == View.VISIBLE){
			v.setBackgroundResource(R.drawable.msg_bar_text);
			mViewInput.setVisibility(View.GONE);
			mBtnPressTalk.setVisibility(View.VISIBLE);
			hidePullUpView(mViewExpressionSet, true);
			hidePullUpView(mViewMoreSet, true);
			hideInputMethod();
		}else{
			v.setBackgroundResource(R.drawable.msg_bar_voice);
			mViewInput.setVisibility(View.VISIBLE);
			showInputMethod();
			mBtnPressTalk.setVisibility(View.GONE);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
//		if(hasFocus){
//			hidePullUpView(mViewExpressionSet, false);
//			hidePullUpView(mViewMoreSet, false);
//		}else{
//			hideInputMethod();
//		}
	}
	
	public void setOnEditListener(OnEditListener listener){
		mOnEditListner = listener;
	}
	
	public void onPause(){
		mRecordViewHelper.onPause();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if(visibility == View.VISIBLE){
			mRecordViewHelper.onResume();
		}else{
			mRecordViewHelper.onPause();
		}
	}
	
	public void onRecordStarted() {
	}

	public void onRecordEnded(String strRecordPath) {
		if(mOnEditListner != null){
			mOnEditListner.onSendVoice(strRecordPath);
		}
	}

	public void onRecordFailed(boolean bFailByNet) {
		if(mOnEditListner != null){
			mOnEditListner.onRecordFail(bFailByNet);
		}
	}
	
	protected static class	ExpressionTab{
		public View 	mTabButton;
		public boolean	mIsTabSelectable;
		public View		mTabContent;
	}
	
	public static interface OnEditListener{
		public boolean 	onSendCheck();
		
		public void		onRecordFail(boolean bFailByNet);
		
		public void 	onSendText(CharSequence s);
		
		public void	 	onSendVoice(String strPathName);
		
		public void		onSendPlugin(SendPlugin sp);
	}
}
