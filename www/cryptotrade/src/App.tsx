import React from 'react';
import logo from './logo.svg';
import axios, { CancelTokenSource } from 'axios';
import './App.css';
import IOhlcv from './types/Ohlcv';
import { resolveProjectReferencePath } from 'typescript';
const defaultCandles: IOhlcv[] = [];

function App() {
  const [candles, setCandles]:[IOhlcv[], (candles: IOhlcv[])=>void] = React.useState(defaultCandles) ;
  const [loading, setLoading]: [
    boolean,
    (loading: boolean) => void
  ] = React.useState<boolean>(true);
  const [error, setError]: [string, (error: string) => void] = React.useState(
    ''
  );
  const cancelToken = axios.CancelToken; //create cancel token
  const [cancelTokenSource, setCancelTokenSource]: [
    CancelTokenSource,
    (cancelTokenSource: CancelTokenSource) => void
  ] = React.useState(cancelToken.source());

  const restUrl="http://localhost:8080/candles/list";
  React.useEffect(() => {

    fetch(restUrl)
      .then(responce=>responce.json())
      .then((candles:IOhlcv[])=>{
          setCandles(candles)
      })
  }, []);  

  return (
    <div className="App">
      <div>
        <h1>Candles</h1>
        <ul>
          {candles.map((candle)=>(
            <li key = {candle.datetime}>
              {candle.datetime}, {candle.open}, {candle.high}, {candle.low}, {candle.close}
            </li>
          ))}
        </ul>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}

export default App;
