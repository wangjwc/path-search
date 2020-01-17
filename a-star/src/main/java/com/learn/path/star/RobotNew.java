package com.learn.path.star;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RobotNew {
    //行数
    public static int colomnNum;

    //列数
    public static int rowNum;

    //障碍物数量
    public static int obstacleNum;

    //用于存放state的优先级队列
    public static Queue<State> priorityQueue;

    //地图
    public static String[][] map;
    //灰尘坐标列表
    public static List<Point> dirtList;

    //closeList，用于存放已经存在的state
    public static List<State> closeList;

    //遍历总耗费
    public static int cost = 0;

    public static String[][] loadMap(State robotState) throws IOException {
        InputStream inputStream = RobotNew.class.getResourceAsStream("/map");
        StringBuilder sb = new StringBuilder();
        try {
            byte[] buf = new byte[1024];
            int i = -1;
            while ((i = inputStream.read(buf)) != -1) {
                sb.append(new String(buf, 0, i, StandardCharsets.UTF_8));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] lines = sb.toString().trim().split("\n");
        int colNum = lines[0].trim().length();

        String[][] map = new String[lines.length][colNum];


        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() != colNum) {
                throw new RuntimeException(String.format("map error %d = %d != %d", i, lines[i].length(), colNum));
            }

            for (int j = 0; j < colNum; j++) {
                //统计障碍物数量
                if (line.charAt(j) == '#') {
                    obstacleNum++;
                }

                //将灰尘格子坐标存入list中
                if (line.charAt(j) == '*') {
                    dirtList.add(new Point(i, j));
                }

                //设置机器人初始坐标
                if (line.charAt(j) == '@') {
                    robotState.setRobotLocation(new Point(i, j));
                }

                //初始化地图
                map[i][j] = String.valueOf(line.charAt(j));
            }
        }

        return map;
    }

    public static void main(String[] args) throws IOException {
        State initialState = new State();
        dirtList = new ArrayList<Point>();
        closeList = new ArrayList<State>();

        map = loadMap(initialState);
        rowNum = map.length;
        colomnNum = map[0].length;


        initialState.setDirtList(dirtList);
        initialState.setCost(0);
        initialState.setFvalue(dirtList.size());

        //优先级队列的自定义Comparator,比较规则是Fvalue较小的state排在队列前面
        //初始化优先级队列
        priorityQueue = new PriorityQueue<>(5, Comparator.comparingDouble(State::getFvalue));

        closeList.add(initialState);
        priorityQueue.add(initialState);
        cost++;

        //遍历开始
        while (!priorityQueue.isEmpty()) {
            //取出队列中第一个state
            State state = priorityQueue.poll();

            //如果达到目标,输出结果并退出
            if (isgoal(state)) {
                output(state);
                return;
            }
            calculate(state);
        }
    }

    public static void calculate(State state) {
        //获取当前机器人的坐标
        int x = state.getRobotLocation().getX();
        int y = state.getRobotLocation().getY();

        //如果当前的点是灰尘并且没有被清理
        if (map[x][y].equals("*") && !isCleared(new Point(x, y), state.getDirtList())) {
            State newState = new State();
            List<Point> newdirtList = new ArrayList<Point>();
            //在新的state中,将灰尘列表更新,即去掉当前点的坐标
            for (Point point : state.getDirtList()) {
                if (point.getX() == x && point.getY() == y) {
                    continue;
                } else {
                    newdirtList.add(new Point(point.getX(), point.getY()));
                }
            }
            newState.setDirtList(newdirtList);
            // gValue + 1
            newState.setCost(state.getCost() + 1);
            //Fvalue为gValue和hValue的和
            newState.setFvalue(newState.getCost() + newdirtList.size());
            newState.setRobotLocation(new Point(x, y));
            //C代表Clean操作
            newState.setOperation("C");
            newState.setPreviousState(state);

            //若新产生的状态与任意一个遍历过的状态都不同,则进入队列
            if (!isDuplicated(newState)) {
                priorityQueue.add(newState);
                closeList.add(newState);
                cost++;
            }
            //printPath(newState);
        }

        // 上
        //若当前机器人坐标上方有格子并且不是障碍物
        if (x - 1 >= 0) {
            if (!map[x - 1][y].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x - 1, y));
                //N代表North,即向上方移动一个格子
                newState.setOperation("↑");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    closeList.add(newState);
                    cost++;
                }
            }
        }

        // 右上
        if (x - 1 >= 0 && y + 1 < colomnNum) {
            if (!map[x - 1][y + 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x - 1, y + 1));
                newState.setOperation("↗");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    closeList.add(newState);
                    cost++;
                }
            }
        }


        //右
        //若当前机器人坐标右侧有格子并且不是障碍物
        if (y + 1 < colomnNum) {
            if (!map[x][y + 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x, y + 1));
                //E代表East,即向右侧移动一个格子
                newState.setOperation("→");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    closeList.add(newState);
                    cost++;
                }
            }
        }

        //右下
        if (x + 1 < rowNum && y + 1 < colomnNum) {
            if (!map[x + 1][y + 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x + 1, y + 1));
                //E代表East,即向右侧移动一个格子
                newState.setOperation("↘");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    closeList.add(newState);
                    cost++;
                }
            }
        }


        // 下
        //若当前机器人坐标下方有格子并且不是障碍物
        if (x + 1 < rowNum) {
            if (!map[x + 1][y].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x + 1, y));
                //S代表South,即向下方移动一个格子
                newState.setOperation("↓");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    //加入到closeList中
                    closeList.add(newState);
                    cost++;
                }
            }
        }

        //左下
        if (x + 1 < rowNum && y - 1 >= 0) {
            if (!map[x + 1][y - 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x + 1, y - 1));
                //S代表South,即向下方移动一个格子
                newState.setOperation("↙");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    //加入到closeList中
                    closeList.add(newState);
                    cost++;
                }
            }
        }

        // 左
        //若当前机器人坐标左侧有格子并且不是障碍物
        if (y - 1 >= 0) {
            if (!map[x][y - 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x, y - 1));
                //W代表West,即向左侧移动一个格子
                newState.setOperation("←");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    closeList.add(newState);
                    cost++;
                }
            }
        }

        // 左上
        if (x - 1 >=0 && y - 1 >= 0) {
            if (!map[x - 1][y - 1].equals("#")) {
                State newState = new State();
                newState.setDirtList(state.getDirtList());
                newState.setRobotLocation(new Point(x - 1, y - 1));
                //S代表South,即向下方移动一个格子
                newState.setOperation("↖");
                newState.setCost(state.getCost() + 1);
                newState.setFvalue(newState.getCost() + getHValue(newState));
                newState.setPreviousState(state);
                if (!isDuplicated(newState)) {
                    priorityQueue.add(newState);
                    //加入到closeList中
                    closeList.add(newState);
                    cost++;
                }
            }
        }



    }

    private static double getHValue(State currentState) {
        //return currentState.getDirtList().size();

        if (currentState.getDirtList().size() == 1) {
            return 1;
        }

        Point currentStatePoint = currentState.getRobotLocation();

        List<Double> ls = currentState.getDirtList().stream()
                .map(d -> Math.sqrt(Math.pow(d.getX() - currentStatePoint.getX(), 2) + Math.pow(d.getY() - currentStatePoint.getY(), 2)))
                .collect(Collectors.toList());

        return ls.stream().reduce(Double::sum).orElse(0.0);
//        double avg = ls.stream().reduce(Double::sum).orElse(0.0) / ls.size();

//
//        double variance = 0.0;
//        for (Double d : ls) {
//            variance += (d - avg) * (d - avg);
//        }
//
//        return variance / ls.size() - 1;

    }

    //判断是否已经达到目标,即当前遍历到的state中手否已经没有灰尘需要清理
    public static boolean isgoal(State state) {
        if (state.getDirtList().isEmpty()) {
            return true;
        }
        return false;
    }

    //输出,由最后一个state一步一步回溯到起始state
    public static void output(State state) {
        printPath(state);

        String output = "";
        //回溯期间把每一个state的操作(由于直接输出的话是倒序)加入到output字符串之前,再输出output
        while (state != null) {
            if (state.getOperation() != null) {
                output = state.getOperation() + "\r\n" + output;
            }
            state = state.getPreviousState();
        }
        System.out.println(output);
        //最后输出遍历过的节点(state)数量
        System.out.println(cost);
    }

    //判断节点是否存在,即将state与closeList中的state相比较,若都不相同则为全新节点
    public static boolean isDuplicated(State state) {
        for (State state2 : closeList) {
            if (State.isSameState(state, state2)) {
                return true;
            }
        }
        return false;
    }

    //判断地图中当前位置的灰尘在这个state中是否已经被除去。
    public static boolean isCleared(Point point, List<Point> list) {
        for (Point p : list) {
            if (Point.isSamePoint(p, point)) {
                return false;
            }
        }
        return true;
    }

    public static void printMap() {
        StringBuilder sb = new StringBuilder();
        for (String[] strings : map) {
            for (String string : strings) {
                sb.append(" ").append(string);
                if ("↗↘↙↖".indexOf(string) == -1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    public static void printPath(State state) {
        Stack<State> stack = new Stack<>();
        while (state != null) {
            stack.push(state);
            state = state.getPreviousState();
        }

        State s = stack.pop();
        while (!stack.empty()) {
            State next = stack.pop();

            Point p = s.getRobotLocation();

            if (null != next.getOperation()) {
                map[p.getX()][p.getY()] = next.getOperation();
            }
            s = next;
            printMap();
        }

//        State preState;
//
//        while (state != null) {
//            preState = state.getPreviousState();
//            if (state.getOperation() != null) {
//                int preX = preState.getRobotLocation().getX();
//                int preY = preState.getRobotLocation().getY();
//                if (!"*".equals(map[preX][preY])) {
//                    map[preX][preY] = state.getOperation();
//                }
//                printMap();
//            }
//
//            state = preState;
//        }

        printMap();
    }

}

