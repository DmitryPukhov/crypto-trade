export default interface IOhlcv {
    datetime: string,
    symbol: string,
    open: string,
    high: number,
    low: number,
    close: number,
    vol: number
}