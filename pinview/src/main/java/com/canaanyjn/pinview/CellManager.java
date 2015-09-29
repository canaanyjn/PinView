package com.canaanyjn.pinview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by canaan on 2015/9/23 0023.
 */
public class CellManager {
    private List<Cell> mCells = new ArrayList<>();
    private float rowDistance;
    private float colummDistance;

    private int cellRowNumber = 3;
    private int cellColummNumber = 3;
    private final int cellRadius = 20;
    private int cellColor;
    private float slop;

    private float startX,startY;

    public CellManager(int cellRowNumber,int cellColummNumber,float cellRadius) {
        slop = 4*cellRadius;
    }

    public void initCells() {
        float x = startX,y = startY;
        mCells.clear();
        for (int i = 0;i<cellRowNumber;i++) {
            for (int j = 0;j<cellColummNumber;j++) {
                Cell cell = new Cell();
                cell.setRadius(cellRadius);
                cell.setX(x);
                cell.setY(y);
                mCells.add(cell);

                if (j == cellColummNumber-1) {
                    y += rowDistance;
                    x = startX;
                } else {
                    x += colummDistance;
                }
            }
        }
    }

    public int setDownPosition(float x,float y) {
        for (int i = 0; i<mCells.size();i++) {
            Cell cell = mCells.get(i);
            if (cell.getY() > y - slop && cell.getY() < y + slop
                    && cell.getX() < x + slop && cell.getX() > x - slop) {
                return i;
            }
        }
        return -1;
    }

    public List<Cell> getCells() {
        return mCells;
    }


    public int getCellRowNumber() {
        return cellRowNumber;
    }

    public int getCellColummNumber() {
        return cellColummNumber;
    }

    public int getCellRadius() {
        return cellRadius;
    }

    public int getCellColor() {
        return cellColor;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public void setRowDistance(float rowDistance) {
        this.rowDistance = rowDistance;
    }

    public void setColummDistance(float colummDistance) {
        this.colummDistance = colummDistance;
    }
}
