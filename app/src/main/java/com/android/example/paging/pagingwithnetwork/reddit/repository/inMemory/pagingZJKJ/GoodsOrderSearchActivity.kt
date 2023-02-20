package com.sgb.goods.view.activity.details.order

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.SizeUtils
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.reflect.TypeToken
import com.sgb.goods.BR
import com.sgb.goods.R
import com.sgb.goods.databinding.GoodsActivityOrderSearchBinding
import com.sgb.goods.entity.SearchTagEntity
import com.sgb.goods.utils.OrderFileUtils
import com.sgb.goods.utils.base.GoodsImmersionActivity
import com.sgb.goods.view.activity.SearchShopFirstActivity
import com.sgb.goods.view.adapter.order.goods.OrderSearchPagingAdapter
import com.sgb.goods.viewmodel.details.order.GoodsOrderSearchViewModel
import com.sgb.goods.widget.MyTextWatcher
import com.zjkj.lib.utils.common.toast.MToast
import com.zjkj.route.path.goods.PathGoods
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * @author qiucheng
 * 采购，销售订单列表搜索
 */
@Route(path = PathGoods.AR_GOODS_ORDER_SEARCH)
class GoodsOrderSearchActivity : GoodsImmersionActivity<GoodsActivityOrderSearchBinding, GoodsOrderSearchViewModel>() {
    override fun initContentView(p0: Bundle?): Int = R.layout.goods_activity_order_search

    /**
     * 订单流程标题|订单状态|是否审批单(是审批单传1，否则传空字符串)
     */
    private lateinit var orderArray: Array<Array<String>>
    private val pagingAdapter = OrderSearchPagingAdapter()

    companion object {
        const val PURCHASE_FILENAME = "PURCHASE_TXT"
        const val SELL_FILENAME = "SELL_TXT"

        /**
         * 保存json到本地
         *
         * @param mActivity
         * @param filename
         * @param content
         */
        var dirPurchase = File(Environment.getExternalStorageDirectory().toString() + "/.orderList/json/")
        var dirSell = File(Environment.getExternalStorageDirectory().toString() + "/.orderList/json/")
    }

    override fun initView() {
        orderArray = arrayOf(arrayOf("全部", "", ""), arrayOf("审批单", "", "1"),
                arrayOf("待接单", "0", ""), arrayOf("待签约/确认", "99", ""),
                arrayOf("履约中", "10", ""), arrayOf("已完成", "7", ""),
                arrayOf("已取消", "8", ""))

        initEtSearchView()
        initSearchTag()
        binding.goodsRlSearchPaging.layoutManager = LinearLayoutManager(this)
        binding.goodsRlSearchPaging.adapter = pagingAdapter
//        binding.goodsRlSearchPaging.adapter = pagingAdapter.withLoadStateHeaderAndFooter(
//                header = ,
//                footer =
//        )
    }

    private fun initSearchTag() {
        if (TextUtils.isEmpty(readJsonTxt(viewModel.orderType))) {
            binding.searchTagList.visibility = View.GONE
            binding.layoutSearch.visibility = View.GONE
        } else {
            setSearchTagList()
            binding.layoutSearch.visibility = View.VISIBLE
            binding.searchTagList.visibility = View.VISIBLE
        }
        binding.searchTagList.maxLine = 5
        binding.searchTagList.flexWrap = FlexWrap.WRAP
    }

    override fun initViewModel() {
        viewModel = createViewModel(GoodsOrderSearchViewModel::class.java, BR.viewModel)
    }

    /**
     * 初始化 搜索功能
     */
    private fun initEtSearchView() {
        binding.etView.isFocusable = true
        binding.etView.isFocusableInTouchMode = true
        binding.etView.requestFocus()
        binding.etView.imeOptions = EditorInfo.IME_ACTION_SEARCH

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(binding.etView, 0)
            }
        },200)
    }

    private fun setSearchTagList() {
        viewModel.mSearchData = SearchShopFirstActivity.JsonToObject(readJsonTxt(viewModel.orderType), object : TypeToken<List<String?>?>() {}.type)
        viewModel.mSearchData.forEach {
            val searchTagEntity: SearchTagEntity<*> = SearchTagEntity<Any?>()
            searchTagEntity.dmmc = it
            viewModel.mSearchTagEntities.add(searchTagEntity)
        }
        viewModel.mSearchTagEntities.forEach {
            binding.searchTagList.addView(createNewFlexItemTextView(it.dmmc, false))
        }
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { v -> finish() }
        binding.tvSearch.setOnClickListener { v ->
            if (TextUtils.isEmpty(binding.etView.text.toString().trim())) {
                MToast.showToast("请输入关键字")
            } else {
                doSearch()
            }
        }
        binding.deleteImg.setOnClickListener {
            binding.layoutSearch.visibility = View.GONE
            binding.searchTagList.visibility = View.GONE
            binding.searchTagList.removeAllViews()
            binding.searchTagList.removeAllViewsInLayout()
            deleteJsonTxt(viewModel.orderType)
        }
        binding.ivSearchDelete.setOnClickListener {
            binding.etView.setText("")
            binding.searchTagList.visibility = View.VISIBLE
            binding.layoutSearch.visibility = View.VISIBLE
            binding.goodsFragmentContainer.visibility = View.GONE
        }
        binding.etView.addTextChangedListener(object : MyTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.ivSearchDelete.visibility = if (TextUtils.isEmpty(s.toString().trim { it <= ' ' })) View.GONE else View.VISIBLE
            }
        })
        binding.etView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                if (TextUtils.isEmpty(binding.etView.text.toString().trim())) {
                    MToast.showToast("请输入关键字")
                    val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(v.windowToken, 0)
                    return@OnKeyListener true
                }
                doSearch()
            }
            false
        })
        binding.refreshLayout.setEnableRefresh(false)

        pagingAdapter.setOnClickListenerPop {

        }
    }

    override fun initViewObservable() {

    }

    override fun initData() {
        this.intent?.let {
            viewModel.orderType = it.getIntExtra("orderType", 0)
        }
    }

    private fun doSearch() {
        val searchText = binding.etView.text.toString().trim()
        binding.searchTagList.visibility = View.GONE
        binding.layoutSearch.visibility = View.GONE
       // binding.goodsFragmentContainer.visibility = View.VISIBLE

        pagingAdapter.orderDescribe = viewModel.orderType.toString()
        pagingAdapter.keyword = searchText
        lifecycleScope.launch {
            viewModel.getPagingData(searchText).collect{
                pagingAdapter.submitData(it)
            }
        }

        pagingAdapter.addLoadStateListener {
            if (it.append.endOfPaginationReached && it.refresh !is LoadState.Loading) {
                // 最后一页。显示没有更多数据了
                binding.refreshLayout.finishLoadMoreWithNoMoreData()
            } else if (it.refresh is LoadState.Loading && it.append is LoadState.NotLoading && it.prepend.endOfPaginationReached) {
                // 下拉刷新。比如显示一个菊花加载框等
                binding.refreshLayout.setNoMoreData(false)
                binding.refreshLayout.setEnableLoadMore(true)

                binding.searchTagList.visibility = View.GONE
                binding.layoutSearch.visibility = View.GONE
                binding.refreshLayout.visibility = View.VISIBLE
                // binding.goodsFragmentContainer.visibility = View.VISIBLE
                binding.goodsEmptyView.root.visibility = View.GONE
            } else if (it.refresh is LoadState.Error) {
                //没有数据
                binding.searchTagList.visibility = View.GONE
                binding.layoutSearch.visibility = View.GONE
                binding.refreshLayout.visibility = View.GONE
                // binding.goodsFragmentContainer.visibility = View.VISIBLE
                binding.goodsEmptyView.root.visibility = View.VISIBLE
            } else {
                // ... 其他的情况
            }
        }

        lifecycleScope.launchWhenCreated {
            pagingAdapter.loadStateFlow
                    // Only emit when REFRESH LoadState for RemoteMediator changes.
                    .distinctUntilChangedBy { it.refresh }
                    // Only react to cases where REFRESH completes, such as NotLoading.
                    .filter { it.refresh is LoadState.NotLoading }
                    // Scroll to top is synchronous with UI updates, even if remote load was
                    // triggered.
                    .collect {
                        binding.goodsRlSearchPaging.scrollToPosition(0)
                        binding.searchTagList.visibility = View.GONE
                        binding.layoutSearch.visibility = View.GONE
                        binding.refreshLayout.visibility = View.VISIBLE
                        // binding.goodsFragmentContainer.visibility = View.VISIBLE
                        binding.goodsEmptyView.root.visibility = View.GONE
                    }
        }
//        supportFragmentManager.beginTransaction().replace(R.id.goods_fragment_container,
//                OrderListFragment(viewModel.orderType.toString(), false, "全部", "", "", searchText)).commit()
        setJsonTxt(viewModel.orderType, searchText)
        if (!viewModel.mSearchData.contains(searchText)) {
            binding.searchTagList.addView(createNewFlexItemTextView(searchText, false))
        }

        closeKeyBord()
    }

    private fun closeKeyBord() {
        currentFocus?.let {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun setJsonTxt(orderType: Int, text: String) {
        if (orderType == 1) {
            OrderFileUtils.setJsonTxt(text, this, dirPurchase, PURCHASE_FILENAME)
        } else if (orderType == 2) {
            OrderFileUtils.setJsonTxt(text, this, dirSell, SELL_FILENAME)
        }
    }

    private fun deleteJsonTxt(orderType: Int) {
        if (orderType == 1) {
            OrderFileUtils.deleteDirectory(dirPurchase, PURCHASE_FILENAME)
        } else if (orderType == 2) {
            OrderFileUtils.deleteDirectory(dirSell, SELL_FILENAME)
        }
    }

    private fun readJsonTxt(orderType: Int): String {
        return if (orderType == 1) {
            OrderFileUtils.readTextFile(dirPurchase, PURCHASE_FILENAME)
        } else if (orderType == 2) {
            OrderFileUtils.readTextFile(dirSell, SELL_FILENAME)
        } else {
            ""
        }
    }
    /**
     * 动态创建TextView
     *
     * @param entity
     * @return
     */
    private fun createNewFlexItemTextView(dmmc: String, ischeck: Boolean): TextView {
        val textView = TextView(this)
        textView.gravity = Gravity.CENTER
        textView.text = dmmc
        textView.textSize = 12f
        if (ischeck) {
            textView.setTextColor(resources.getColor(R.color.res_color_FFFF7C38))
        } else {
            textView.setTextColor(resources.getColor(R.color.res_color_FF333333))
        }
        textView.setBackgroundResource(R.drawable.goods_corner5_white_bg)
        textView.setLines(1)
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setOnClickListener {
            binding.etView.setText(dmmc)
            doSearch()
        }
        val padding = SizeUtils.dp2px(5f)
        val paddingLeftAndRight = SizeUtils.dp2px(8f)
        val Height = SizeUtils.dp2px(28f)
        ViewCompat.setPaddingRelative(textView, paddingLeftAndRight, 0, paddingLeftAndRight, 0)
        val layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Height)
        layoutParams.setMargins(padding, padding, padding, 0)
        textView.layoutParams = layoutParams
        return textView
    }
}