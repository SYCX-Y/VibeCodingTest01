package com.example.jobapplication.common;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 导出列宽自适应处理器：
 * 遍历表头与数据单元格，按每列内容的最大字符宽度（中文按 2 字符计）动态设置列宽，
 * 保证导出后长文本（公司名 / 投递时间 / 备注等）完整显示。
 */
public class AutoColumnWidthHandler implements CellWriteHandler {

    /** 每列当前最大内容宽度（字符数） */
    private final Map<Integer, Integer> maxWidthMap = new HashMap<>();

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        int colIndex = cell.getColumnIndex();
        int contentWidth = computeTextWidth(getCellValue(cell));
        int max = Math.max(maxWidthMap.getOrDefault(colIndex, 0), contentWidth);

        // 表头文字同样计入列宽
        if (isHead && head != null && head.getHeadNameList() != null && !head.getHeadNameList().isEmpty()) {
            int headWidth = computeTextWidth(head.getHeadNameList().get(0));
            max = Math.max(max, headWidth);
        }
        maxWidthMap.put(colIndex, max);

        // 列宽 = 内容宽度 + 2 字符余量，限制在 [6, 255] 字符（Excel 上限）；POI 单位为 1/256 字符宽
        int charWidth = Math.min(Math.max(max + 2, 6), 255);
        writeSheetHolder.getSheet().setColumnWidth(colIndex, charWidth * 256);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (type == CellType.NUMERIC) {
            double v = cell.getNumericCellValue();
            if (v == Math.floor(v) && !Double.isInfinite(v)) {
                return String.valueOf((long) v);
            }
            return String.valueOf(v);
        }
        if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private int computeTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int width = 0;
        for (char c : text.toCharArray()) {
            width += c > 0xFF ? 2 : 1;
        }
        return width;
    }
}
