package qr_reader_logic.abhi.barcode.frag.libv2_modified;

import com.google.zxing.Result;

public class ScanResult {

    private Result rawResult;

    public ScanResult(Result rawResult) {
        this.rawResult = rawResult;
    }

    public Result getRawResult() {
        return rawResult;
    }

}
