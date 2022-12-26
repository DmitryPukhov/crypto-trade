
import './pricechart.css'
import React,{Component} from 'react'
import Plotly from 'plotly.js'
import createPlotlyComponent from "react-plotly.js/factory"
import IOhlcv from '../types/Ohlcv'
import { timingSafeEqual } from 'crypto'
import config from './../app-config.json'

const Plot = createPlotlyComponent(Plotly);

type PriceChartProps = {};


export default class PriceChart extends Component<PriceChartProps>{

  state = {
    error: null,
    isLoaded: false,
    items: []
  };

  layout = {  title: 'BTC/USDT prices',
    xaxis:  { autorange: true},
    yaxis:  { autorange: true}
  };
  serviceUrl=""

  plotlyDataOf(candles: Array<any>)  {
      let x=candles.map(candle=>candle.datetime)
      let y = candles.map(candle=>candle.close)
      return [{x:x, y:y, mode: "markers", type: "scatter"}]
  }

  constructor(props: PriceChartProps) {
    super(props)
    // todo: parameterise
    //const serviceUrl="http://localhost:8080/candles/list";
    this.serviceUrl=`${config.REACT_APP_CRYPTOTRADE_SERVICE_URL}/candles/list`
    console.log(`Service url: ${this.serviceUrl}`)
    console.log(props)

    fetch(this.serviceUrl)
    .then(responce=>responce.json())
    .then((candles:IOhlcv[])=>{
        this.setState({isLoaded: true, items: this.plotlyDataOf(candles), error: null});
    })
    .catch((error)=>{
      this.setState({isLoaded: false, items:[],error: error.message});
    });
    
  }

  render(){
          const { error, isLoaded, items } = this.state;
          if (error) {
            return <div>
              <div>{this.serviceUrl}</div>
              <div>Error: {error}</div>
              </div>;
          } else if (!isLoaded) {
            return <div>Loading...</div>;
          } else { 
            return(            
          <div>
            <Plot layout={this.layout} data={this.state.items} ></Plot>
          </div> );
        }
      }
   
  }