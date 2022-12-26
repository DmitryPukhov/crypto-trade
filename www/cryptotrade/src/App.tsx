import React from 'react';
import logo from './logo.svg';
import axios, { CancelTokenSource } from 'axios';
import './App.css';
import ITickerPrice from './types/TickerPrice';
import { resolveProjectReferencePath } from 'typescript';
import PriceChart from './pricechart/PriceChart';

const defaultCandles: ITickerPrice[] = [];

function App() {
  const plotly = React.createRef();

  return (
    <div className="App">
      <div>
        <h1>Price analysis</h1>
        <div id='priceChart'>
          <PriceChart />
        </div>
      </div>
    </div>
  );
}

export default App;
