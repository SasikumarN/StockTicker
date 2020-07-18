package com.github.premnirmal.ticker.news

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.premnirmal.ticker.base.BaseGraphActivity
import com.github.premnirmal.ticker.components.Injector
import com.github.premnirmal.ticker.isNetworkOnline
import com.github.premnirmal.ticker.model.IHistoryProvider.Range
import com.github.premnirmal.ticker.network.data.Quote
import com.github.premnirmal.ticker.showDialog
import com.github.premnirmal.tickerwidget.R
import kotlinx.android.synthetic.main.activity_graph.desc
import kotlinx.android.synthetic.main.activity_graph.graph_holder
import kotlinx.android.synthetic.main.activity_graph.max
import kotlinx.android.synthetic.main.activity_graph.one_month
import kotlinx.android.synthetic.main.activity_graph.one_year
import kotlinx.android.synthetic.main.activity_graph.progress
import kotlinx.android.synthetic.main.activity_graph.three_month
import kotlinx.android.synthetic.main.activity_graph.tickerName
import kotlinx.android.synthetic.main.activity_graph.two_weeks

class GraphActivity : BaseGraphActivity() {

  companion object {
    const val TICKER = "TICKER"
    private const val DURATION = 2000
  }

  override val simpleName: String = "GraphActivity"
  private lateinit var ticker: String
  protected lateinit var quote: Quote
  private lateinit var viewModel: GraphViewModel
  override var range: Range = Range.THREE_MONTH

  override fun onCreate(savedInstanceState: Bundle?) {
    Injector.appComponent.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_graph)
    setupGraphView()
    ticker = checkNotNull(intent.getStringExtra(TICKER))
    viewModel = ViewModelProvider(this, AndroidViewModelFactory.getInstance(application))
        .get(GraphViewModel::class.java)
    viewModel.quote.observe(this, Observer { quote ->
      this.quote = quote
      tickerName.text = ticker
      desc.text = quote.name
    })
    viewModel.data.observe(this, Observer { data ->
      dataPoints = data
      loadGraph(ticker)
    })
    viewModel.error.observe(this, Observer {
      showErrorAndFinish()
    })
    viewModel.fetchStock(ticker)
    var view: View? = null
    when (range) {
      Range.TWO_WEEKS -> view = two_weeks
      Range.ONE_MONTH -> view = one_month
      Range.THREE_MONTH -> view = three_month
      Range.ONE_YEAR -> view = one_year
      Range.MAX -> view = max
    }
    view?.isEnabled = false
  }

  override fun onStart() {
    super.onStart()
    if (dataPoints == null) {
      fetchGraphData()
    } else {
      loadGraph(ticker)
    }
  }

  override fun fetchGraphData() {
    if (isNetworkOnline()) {
      graph_holder.visibility = View.GONE
      progress.visibility = View.VISIBLE
      viewModel.fetchHistoricalDataByRange(ticker, range)
    } else {
      showDialog(getString(R.string.no_network_message),
          DialogInterface.OnClickListener { _, _ -> finish() }, cancelable = false)
    }
  }

  override fun onGraphDataAdded(graphView: LineChart) {
    progress.visibility = View.GONE
    graph_holder.visibility = View.VISIBLE
    graphView.animateX(DURATION, Easing.EasingOption.EaseInOutCubic)
  }

  override fun onNoGraphData(graphView: LineChart) {
    progress.visibility = View.GONE
    graph_holder.visibility = View.VISIBLE
  }
}