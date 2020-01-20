package com.unitec.xiaofenglin.andraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiaofenglin on 15/04/18.
 */
// This is the canvas class where drawing happens
public class CanvasView extends View
{
    private Paint paint; // for settings of drawing
    private ArrayList<Point> points; // stores points in a stroke
    private ArrayList<Integer> brushSizes; // stores brush sizes for strokes
    private ArrayList<Integer> colors; // stores colors for strokes
    private ArrayList<ArrayList<Point>> strokes; // stores strokes
    private int countStroke = 0; // number of strokes on current canvas; could be less than maxStrokes due to undoing
    private int maxStroke = 0; // number of strokes stored
    private int brushSize = 25; // current brush size
    private Timer longPressTimer; // times a long press duration
    private final int longPressBegin = 500; // delay after which a long press begins; 0.5 s
    private final int longPressPeriod = 500; // period in which a long press task cycles; 0.5 s
    private boolean isLongPress = false; // indicates a long press
    private Point longPressPoint; // stores the point where a long press happens for drawing a big circle to show a user the colors to choose from
    private boolean isRandomColor = false; // indicates the color of drawing is random
    private int tempColor = 0; // stores the color before switching to the random color mode in order to restore the color after switching back
    private int duration = 0; // duration in integer, used to cycle colors on in a long press
    private int colorNum = Color.BLACK; // current color of drawing; zero stands for random colors
    private boolean hasRemovedFirstPoint = false; // indicates the point made on a touch down has been removed if a long press has followed the touch

    // Inner class for create a point with float numbers of coordinates
    class Point
    {
        float x;
        float y;

        public Point(float x, float y)
        {
            this.x = x;
            this.y = y;
        }
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        paint = new Paint();
        strokes = new ArrayList<>();
        brushSizes = new ArrayList<>();
        colors = new ArrayList<>();
        paint.setAntiAlias(true); // eliminates jaggies on a stroke;
        paint.setColor(Color.BLACK); // sets the default color of drawing
        paint.setStyle(Paint.Style.FILL); // sets the paint style to fill because in this app we use filled circles to make strokes
    }

    // To be invoked in the MainActivity to set the brush size
    public void setBrushSize(int brushSize)
    {
        this.brushSize = brushSize;
    }

    // To be invoked in the MainActivity to set the color mode
    public void setColorRandom(boolean isRandomColor)
    {
        this.isRandomColor = isRandomColor;
        if (isRandomColor)
        {
            tempColor = colorNum; // stores the current drawing color before switching to the random color mode
        }
        else
        {
            colorNum = tempColor; // restores the previous drawing color after switching back to the single color mode
        }
    }

    // To be invoked in the MainActivity to execute an undo
    public boolean undoable()
    {
        if (countStroke > 0)
        {
            countStroke--;
            invalidate(); // asks to redraw
        }
        else
        {
            countStroke = 0;
            return false;
        }

        return true;
    }

    // To be invoked in the MainActivity to execute an redo
    public boolean redoable()
    {
        if (countStroke < strokes.size())
        {
            countStroke++;
            invalidate();
        }
        else
        {
            countStroke = strokes.size();
            return false;
        }

        return true;
    }

    // To be invoked in the MainActivity to check there is content to remove
    public boolean clearable()
    {
        if (countStroke < 1)
        {
            countStroke = 0;
            return false;
        }

        return true;
    }

    // To be invoked in the MainActivity to execute a clearance
    public void clear()
    {
        countStroke = 0;
        maxStroke = 0;
        strokes.clear();
        brushSizes.clear();
        colors.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // Draw the color-indicating circle
        if (isLongPress) // checks it is a long press
        {
            canvas.drawCircle(longPressPoint.x, longPressPoint.y, 200, paint); // draws a large filled circle to show the colors for the user to choose from
        }

        // Draw the strokes
        for(int j = 0; j < countStroke; j++)
        {
            ArrayList<Point> stroke = strokes.get(j);
            int radius = brushSizes.get(j);
            int colorStroke = colors.get(j);
            paint.setColor(colorStroke);

            // Draw an individual stroke
            for(int i = 0; i < stroke.size(); i++)
            {
                // Use random colors if in the random color mode
                if (colorStroke == 0)
                {
                    switch (i % 8)
                    {
                        case 1:
                            paint.setColor(Color.RED);
                            break;
                        case 2:
                            paint.setColor(Color.YELLOW);
                            break;
                        case 3:
                            paint.setColor(Color.GREEN);
                            break;
                        case 4:
                            paint.setColor(Color.CYAN);
                            break;
                        case 5:
                            paint.setColor(Color.MAGENTA);
                            break;
                        case 6:
                            paint.setColor(Color.BLUE);
                            break;
                        case 7:
                            paint.setColor(Color.GRAY);
                            break;
                        default:
                            paint.setColor(Color.BLACK);
                    }
                }

                canvas.drawCircle(stroke.get(i).x, stroke.get(i).y, radius, paint); // draws a filled circle
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int pointerIndex = event.getActionIndex(); // gets the pointer index from the event object
        int maskedAction = event.getActionMasked(); // gets a masked (not specific to a pointer) action
        // Get the coordinates of the point touched on
        float posX = event.getX(pointerIndex);
        float posY = event.getY(pointerIndex);
        longPressPoint = new Point(posX, posY); // in case this be a long press touch

        switch(maskedAction)
        {
            case MotionEvent.ACTION_DOWN:
                // In the random color mode
                if (!isRandomColor)
                {
                    longPressTimer = new Timer(); // creates a new timer for the long press
                    longPressTimer.schedule(new TimerTask(){ // creates a new task when the timer runs
                        @Override
                        public void run() { // runs the task
                            isLongPress = true;
                            // Remove the first point touched on because it is not intended if the user just wants to choose a color
                            if (!hasRemovedFirstPoint) // ensures the removing only happens once
                            {
                                strokes.remove(countStroke - 1);
                                brushSizes.remove(countStroke - 1);
                                colors.remove(countStroke - 1);
                                countStroke--;
                                maxStroke = countStroke;
                                hasRemovedFirstPoint = true; // ensures the removing only happens once
                            }
                            // Cycle the colors to choose from
                            switch (duration % 8) // lets the colors cycling
                            {
                                case 1:
                                    colorNum = Color.RED;
                                    break;
                                case 2:
                                    colorNum = Color.YELLOW;
                                    break;
                                case 3:
                                    colorNum = Color.GREEN;
                                    break;
                                case 4:
                                    colorNum = Color.CYAN;
                                    break;
                                case 5:
                                    colorNum = Color.MAGENTA;
                                    break;
                                case 6:
                                    colorNum = Color.BLUE;
                                    break;
                                case 7:
                                    colorNum = Color.GRAY;
                                    break;
                                default:
                                    colorNum = Color.BLACK;
                            }
                            paint.setColor(colorNum);
                            invalidate(); // asks to redraw to show different colors
                            duration++;
                        }
                    }, longPressBegin, longPressPeriod);
                }
                else
                {
                    colorNum = 0; // indicates this is in the random color mode
                }

                points = new ArrayList<>();
                points.add(new Point(posX, posY));
                strokes.subList(countStroke, maxStroke).clear(); // removes the undone strokes first
                strokes.add(points);
                brushSizes.subList(countStroke, maxStroke).clear(); // removes the brush size records of the undone strokes
                brushSizes.add(brushSize);
                colors.subList(countStroke, maxStroke).clear(); // removes the color records  of the undone strokes
                colors.add(colorNum);
                countStroke++;
                maxStroke = countStroke;
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                break;
            }

            case MotionEvent.ACTION_MOVE:
                longPressTimer.cancel(); // cancels the long press
                isLongPress = false; // indicates no long press now
                hasRemovedFirstPoint = false; // indicates no need to remove the first point of a stroke

                int pointerCount = event.getPointerCount(); // gets the number of touches at the moment
                for (int i = 0; i < pointerCount; i++)
                {
                    strokes.get(strokes.size() - 1).add(new Point(event.getX(i), event.getY(i))); // adds the points of each touch creates
                }

                break;

            case MotionEvent.ACTION_UP:
                longPressTimer.cancel();
                isLongPress = false;
                hasRemovedFirstPoint = false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                longPressTimer.cancel();
                isLongPress = false;
                hasRemovedFirstPoint = false;
                break;

            default:
                return false;
        }

        invalidate(); // asks to refresh the canvas

        return true; // indicates the touch event is handled
    }
}
