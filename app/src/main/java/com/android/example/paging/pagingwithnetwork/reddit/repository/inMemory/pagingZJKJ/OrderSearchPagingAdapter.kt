package com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.pagingZJKJ

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SnackbarUtils.getView
import com.chad.library.adapter.base.BaseViewHolder
import com.sgb.goods.BR
import com.sgb.goods.R
import com.sgb.goods.databinding.GoodsItemOrderListBinding
import com.sgb.goods.entity.OrderListEntity
import com.sgb.goods.entity.details.order.CompareContentChildEntity
import com.sgb.goods.widget.pop.OrderListMorePop


class OrderSearchPagingAdapter :
        PagingDataAdapter<OrderListEntity, OrderSearchPagingViewHolder>(OrderSearchPagingComparator) {
    lateinit var binding : GoodsItemOrderListBinding

    /**
     * 订单类型。1：采购订单；2：销售订单。
     */
    var orderDescribe = ""
    private var orderListMorePop: OrderListMorePop? = null
    var keyword = ""

    init {
        if (null == orderListMorePop) {
            orderListMorePop = OrderListMorePop(ActivityUtils.getTopActivity())
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OrderSearchPagingViewHolder {
        val binding: GoodsItemOrderListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.goods_item_order_list, parent, false
        )
        return OrderSearchPagingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderSearchPagingViewHolder, position: Int) {
        val item = getItem(position)
        // Note that item may be null. ViewHolder must support binding a
        // null item as a placeholder.
    //    holder.bind(item)

        binding = holder.getBinding() as GoodsItemOrderListBinding
        if (item != null) {
            binding.setVariable(BR.entity, item.init(orderDescribe))
        }
       // binding.setVariable(BR.adapter, this)
        binding.type = orderDescribe
       holder.addOnClickListener(R.id.tv_pay_info)
        val v = holder.getView(R.id.tv_pay_info) as View
        addOnClickListener(v, item)
//                .addOnClickListener(R.id.tv_deliver_info)
//                .addOnClickListener(R.id.tv_order_confirm)
//                .addOnClickListener(R.id.tv_title)
//                .addOnClickListener(R.id.tv_contract_info)
//                .addOnClickListener(R.id.ll_goods_desc)
//                .addOnClickListener(R.id.iv_sendmessage)
//                .addOnClickListener(R.id.tv_cancel_order)
//                .addOnClickListener(R.id.tv_create_contract)
//                .addOnClickListener(R.id.tv_sign_contract)
//                .addOnClickListener(R.id.tv_immediate_delivery)
//                .addOnClickListener(R.id.tv_apply_pay)
//                .addOnClickListener(R.id.tv_sign_order)
//                .addOnClickListener(R.id.tv_approval_details)
//                .addOnClickListener(R.id.tv_immediate_acceptance)
//                .addOnClickListener(R.id.tv_more)
//                .addOnClickListener(R.id.tv_complete_order)
        if (holder.layoutPosition == 0 && TextUtils.isEmpty(keyword)) {
            holder.getView<View>(R.id.goods_view_scroll).visibility = View.VISIBLE
            keyword = ""
        } else {
            holder.getView<View>(R.id.goods_view_scroll).visibility = View.GONE
        }
    }
    lateinit var clickPop: (Any, View) -> Unit

    fun setOnClickListenerPop(listenr: (Any, View) -> Unit) {
        clickPop = listenr
    }
//    fun <T> singletonList(item: T): List<T> {
//        // ……
//    }
    fun addOnClickListener(view: View,entity: OrderListEntity?){
        if (!view.isClickable) {
            view.isClickable = true
        }

        view.setOnClickListener { v ->
            if (entity != null) {
                clickPop.invoke(entity , v)
            }
        }

        setOnClickListenerPop { any, view ->

        }
    }

}

class OrderSearchPagingViewHolder(binding: GoodsItemOrderListBinding) : BaseViewHolder(binding.root) {
    private var binding: ViewDataBinding? = binding

    fun getBinding(): ViewDataBinding? {
        return binding
    }
}


object OrderSearchPagingComparator : DiffUtil.ItemCallback<OrderListEntity>() {
    override fun areItemsTheSame(oldItem: OrderListEntity, newItem: OrderListEntity): Boolean {
        // Id is unique.
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: OrderListEntity, newItem: OrderListEntity): Boolean {
       // return oldItem == newItem
        return oldItem == newItem
    }
}