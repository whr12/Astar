package myProject;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

/**
 * A星寻路算法v2.0
 * 更新内容：
 *  1. v2.0优化了寻路算法，不再从openList选择最后一个节点去递归，而是从中选择一个离终点曼哈顿距离最近的一个
 *
 * A星寻路算法v2.1
 * 更新内容
 *  1. v2.1优化了路径寻路算法，在v2.0的基础上添加了更多的消耗检测
 *      1.1. 添加移动cost
 *      1.2. 加入转弯cost
 *      1.3. 添加map节点拥堵cost
 */
public class AStarPathSearcherIter {
    private static final int INT = 100;

    public static void main(String[] args) {
        // 数据初始化
        int[][] map = initMap();
        Point start = new Point(4, 3);
        Point end = new Point(24, 36);
        
        System.out.println("迷宫矩阵：");
        for (int a = map.length - 1; a >= 0; a--) {
            for (int b = 0; b < map[0].length; b++) {
                System.out.print(map[a][b] + " ");
            }
            System.out.println();
        }

        System.out.println(String.format("起点:%s, 终点:%s", start.toString().replace("java.awt.Point", ""), end.toString().replace("java.awt.Point", "")));
        System.out.println("正在寻路。。。");
        System.out.println();

        long _s = System.currentTimeMillis();
        PathSearcher searcher = new PathSearcher();
        List<List<Point>> pathAll = searcher.searchPath(map, start, end);
        long _e = System.currentTimeMillis();
        System.out.println("耗时：" + (_e - _s) + "ms");   
        
        System.out.println("迷宫路径：");
        for(List<Point> path:pathAll) {
        	String pathStr = path.stream().map(e -> e.toString().replace("java.awt.Point", "")).collect(Collectors.joining());
        	System.out.println(pathStr);
        }
        
        /**
         * GUI，用于显示地图和路径，能够更加明显看出找出的路径和调试用
         * 由于只是为了调试方便才做的，所以非常粗糙
         * 可删除
         */
        JFrame jf = new JFrame();
        jf.setSize(800,800);
        DrawMap dm = new DrawMap(map,searcher.nodes,pathAll);
        jf.add(dm);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        /******************************************************/
    }

    /**
     * 初始化地图
     *
     * @return
     */
    private static int[][] initMap() {
    	try {
    		//读取MAP.txt文件中的地图数据
			return readMapFile("MAP.txt");
		} catch (IOException e) {
			//读取失败，利用默认的小地图
			int[][] arrs = new int[5][5];
	        arrs[0][0] = 1;
	        arrs[0][1] = 1;
	        arrs[0][2] = 1;
	        arrs[0][3] = 1;
	        arrs[0][4] = 1;
	        arrs[1][0] = 1;
	        arrs[1][1] = 0;
	        arrs[1][2] = 1;
	        arrs[1][3] = 1;
	        arrs[1][4] = 1;
	        arrs[2][0] = 1;
	        arrs[2][1] = 1;
	        arrs[2][2] = 1;
	        arrs[2][3] = 0;
	        arrs[2][4] = 1;
	        arrs[3][0] = 1;
	        arrs[3][1] = 1;
	        arrs[3][2] = 1;
	        arrs[3][3] = 0;
	        arrs[3][4] = 1;
	        arrs[4][0] = 1;
	        arrs[4][1] = 1;
	        arrs[4][2] = 1;
	        arrs[4][3] = 1;
	        arrs[4][4] = 1;

	        return arrs;
		}
        
    }

    /**
     * 读取地图文件信息
     * @param filename
     * @return
     * @throws IOException 
     */
    private static int[][] readMapFile(String filename) throws IOException {
    	BufferedReader in = new BufferedReader(new FileReader(filename));
		String s;
		List<List<Integer>> m = new LinkedList<List<Integer>>();
		while((s=in.readLine())!=null) {
			String[] str = s.split("\t");
			List<Integer> l = new LinkedList<Integer>();
			for(String i:str) {
				int x = Integer.valueOf(i);
				if(x==0) l.add(0);
				else if(x==1) l.add(0);
				else if(x==22) l.add(0);
				else if(x==21) l.add(0);
				else if(x==20) l.add(0);
				else l.add(1);
				
			}
			m.add(l);
		}
		in.close();
		int[][] map = new int[m.size()][m.get(0).size()];
		int i=0;
		int j=0;
		for(List<Integer> l:m) {
			j=0;
			for(int n:l) {
				map[i][j] = n;
				j++;
			}
			i++;
		}
		return map;
	}

	/**
     * A星路径搜索
     *
     * 1. 找出当前节点c周围4个方向能通行的节点，并将可通行的节点放入openList中
     * 	条件：
     * 		1. 不能越界
     * 		2. 不能是已访问过的
     *
     * 2. 从openList里取出最后一个加入的可通行的节点
     *
     * 3. 重复第一步
     *
     */
    private static class PathSearcher {
        private int[][] map;
        private Point start;
        private Point end;
        private LinkedList<AStarNode> openList = new LinkedList<>();
        private List<AStarNode> closeList = new LinkedList<>();

        private AStarNode[][] nodes;
//        private int fCost = Integer.MAX_VALUE;
        /**
         * 搜索路径
         *
         * @param map       地图
         * @param start     起点
         * @param end       终点
         * @return 路径
         */
        public List<List<Point>> searchPath(int[][] map, Point start, Point end) {
        	
            this.map = map;
            this.start = start;
            this.end = end;

            this.nodes = new AStarNode[map.length][map[0].length];
            
            AStarNode current = new AStarNode(null, this.start);
            current.direction = 0xf;
            nodes[start.x][start.y] = current;
            openList.addFirst(current);


            AStarNode currNode = null;
            while ((currNode = getMinCostNode()) != null) {
                System.out.println("minNode:" + currNode + ", cost:" + currNode.getFCost());

                //终点检测
                if(currNode==nodes[end.x][end.y])
                	return getPathList(nodes[end.x][end.y],nodes[end.x][end.y].tCost);
                
                for (int dir = 0; dir < 4; ++dir) {
                    int _dir = Math.abs(dir + currNode.direction) % 4;

                    // 获取下个节点
                    AStarNode nextNode = getNextNode(currNode, _dir);
                    Point next = nextNode.point;
                    
                    // 检测是否越界
                    if (checkIfOutBound(next)) {
                        continue;
                    }

                    // 判断是否为障碍
                    if (this.map[next.x][next.y] == 0) {
                        continue;
                    }

                    // 计算耗费
                    nextNode.hCost = getManhattanDist(next, end);
                    nextNode.tCost = getTurnCost(currNode, nextNode.direction)+currNode.tCost; //累计总的转弯消耗，也用于之后记录最短路径的总转弯次数
                    nextNode.gCost = currNode.gCost + 1;
                    nextNode.bCost = this.map[next.x][next.y];

                    //没有到达nextNode的路径，就添加路径
                    //已有能够到达nextNode的路径，比较总的消耗，相同，就添加一个最短路径，更小，则更新最短路径
                    //如果相差1，则判断与next相连方向的点是否在最短路径中，以此来消除转弯先后对结果的影响
                    if(nodes[next.x][next.y]==null) nodes[next.x][next.y] = nextNode; 
                    else if(nodes[next.x][next.y].getFCost()==nextNode.getFCost()) {
                    	nodes[next.x][next.y].addParent(currNode);
                    	nodes[next.x][next.y].addDirection(nextNode.direction);
                    }                    	
                    else if(nextNode.getFCost()<nodes[next.x][next.y].getFCost())
                    	nodes[next.x][next.y].copyOf(nextNode);
                    else if(nodes[next.x][next.y].getFCost()==nextNode.getFCost()-1){
                    	//判断孙子
                        int grandX = next.x-currNode.point.x+next.x;
                        int grandY = next.y-currNode.point.y+next.y;
                        if(nodes[grandX][grandY]!=null&&nodes[grandX][grandY].parent.contains(nextNode)) {
                        	//孙子在路径中，则添加父亲节点
                        	nodes[next.x][next.y].addParent(currNode);
                        	nodes[next.x][next.y].addDirection(nextNode.direction);
                        }
                    }
                    
                    // 判断是遍历过或已经在openList中，若不在加入openList等待遍历
                    if (!openList.contains(nextNode) && !closeList.contains(nextNode)) {
                        openList.addFirst(nodes[next.x][next.y]);	//添加的节点在最前面，使后进入的点能够先用于验证
                    }
                }

                // 2. 将访问过的节点从openList中移除，并加入到closeList中
                openList.remove(currNode);
                closeList.add(currNode);
            }
            
//            return getPathList(nodes[end.x][end.y],nodes[end.x][end.y].tCost);
            return null;
        }

        /**
         * 获取下一个node
         *
         * @param current
         * @return
         */
        private AStarNode getNextNode(AStarNode current, int dir) {
            Point next = new Point(current.point);
            AStarNode nextNode = new AStarNode(current, next);
            if (dir == 0) {
                next.x += 1;
                nextNode.direction = 0x1;
            } else if (dir == 1) {
                next.y -= 1;
                nextNode.direction = 0x2;
            } else if (dir == 2) {
                next.x -= 1;
                nextNode.direction = 0x4;
            } else if (dir == 3) {
                next.y += 1;
                nextNode.direction = 0x8;
            }

            
            return nextNode;
        }

        /**
         * 判断坐标是否越界
         *
         * @param point
         * @return
         */
        private boolean checkIfOutBound(Point point) {
            if (point.y >= this.map.length
                    || point.x >= this.map[0].length
                    || point.y < 0
                    || point.x < 0) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取两点的曼哈顿距离
         *
         * @param p1
         * @param p2
         * @return
         */
        private int getManhattanDist(Point p1, Point p2) {
            return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
        }

        /**
         * 获取路径的转弯消费
         *
         * @param nextNode
         * @param dir
         * @return
         */
        private int getTurnCost(AStarNode nextNode, int dir) {
            if ((nextNode.direction&dir)!=0) {
                return 0;
            } else {
                return 1;
            }
        }

        /**
         * 从openList中找到cost最小的节点
         * @return
         */
        private AStarNode getMinCostNode() {
            if (openList == null || openList.size() <= 0) {
                return null;
            }

//            return openList.stream().min(Comparator.comparing(e -> e.getFCost())).orElse(null);
            AStarNode min = openList.get(0);
            for (int i = 1; i < openList.size(); ++i) {
                AStarNode next = openList.get(i);
                if (min.getFCost() > next.getFCost()) {
                    min = next;
                }
            }
            return min;
        }
        
        
        /**
         * 获得已查找到的能够到达节点node的所有最短路径的链表
         * 
         * @param node
         * @return
         */
        private List<List<Point>> getPathList(AStarNode node, int tCost){
        	List<List<Point>> path = new LinkedList<List<Point>>();
        	if(node==null) return path;
        	if(tCost==-1) return path;
        	if(node.parent.size()==0) {
        		List<Point> ls = new LinkedList<Point>();
        		ls.add(node.point);
        		path.add(ls);
        		return path;
        	}
        	for(AStarNode p:node.parent) {
        		path.addAll(getPathList(p,node.point,tCost));
        	}
        	
        	
        	return path;
        }
        
        /**
         * 用于辅助解决上个函数的问题
         * @param from  来的节点
         * @param to	要到达的点
         * @param tCost 允许的转弯消耗
         * @return
         */
        private List<List<Point>> getPathList(AStarNode from, Point to, int tCost){
        	List<List<Point>> path = new LinkedList<List<Point>>();
        	if(tCost==-1) return null;	//该路径超过了规定的最大转弯数，不可取
        	if(from==null) {			
        		List<Point> ls = new LinkedList<Point>();
        		ls.add(to);
        		path.add(ls);
        		return path;
        	}
        	if(from.parent.size()==0) {		//起点
        		List<Point> ls = new LinkedList<Point>();
        		ls.add(from.point);
        		ls.add(to);
        		path.add(ls);
        		return path;
        	}
        	for(AStarNode n:from.parent) {
        		List<List<Point>> last;
        		//迭代获取路径
        		if(isLine(n.point,from.point,to)) last = getPathList(n,from.point,tCost);
        		else last = getPathList(n,from.point,tCost-1);
        		if(last!=null) {
        			for(List<Point> lp:last) {
        				lp.add(to);
        				path.add(lp);
        			}
        		}
        	}
        	   	
        	return path;
        }
        
        /**
         * 判断三点是否成线
         * @param p1
         * @param p2
         * @param p3
         * @return
         */
        private boolean isLine(Point p1,Point p2,Point p3) {
        	return (p1.x==p2.x&&p2.x==p3.x) || (p1.y==p2.y&&p2.y==p3.y); 
        }
    }


    /**
     * 路径节点
     */
    private static class AStarNode {
        private List<AStarNode> parent;
        private Point point;
        /** 当前节点的方向 **/
        private int direction;
        /** 从起点到当前节点的耗费 **/
        private int gCost = 0;
        /** 当前节点到终点的预估耗费 **/
        private int hCost = 0;
        /** 转弯耗费 **/
        private int tCost = 0;
        /** 拥堵耗费 **/
        private int bCost = 0;

        public AStarNode() {
        }

        public void copyOf(AStarNode nextNode) {
			this.parent = nextNode.parent;
			this.point = nextNode.point;
			this.direction = nextNode.direction;
			this.gCost = nextNode.gCost;
			this.hCost = nextNode.hCost;
			this.tCost = nextNode.tCost;
			this.bCost = nextNode.bCost;
		}

		public AStarNode(AStarNode parent, Point point) {
//            this.parent = parent;
        	this.parent = new ArrayList<AStarNode>(4);
        	if(parent!=null) this.parent.add(parent);
            this.point = point;
        }

        public int getFCost() {
            return bCost + gCost + hCost + tCost;
        }
        
        public void addParent(AStarNode p) {
        	this.parent.add(p);
        }
        
        public void addDirection(int direction) {
        	this.direction |= direction;
        }

        @Override
        public boolean equals(Object node) {
            if (node instanceof AStarNode) {
                AStarNode pt = (AStarNode) node;
                return point.equals(pt.point);
            }
            return super.equals(node);
        }

        @Override
        public int hashCode() {
            return point != null ? point.hashCode() : 0;
        }

        @Override
        public String toString() {
            return String.format("%s-->%s}", parent.size()==0 ? null : parent.get(parent.size()-1).point, point);
        }
    }
    
    
    /**
     * 画板，不使用GUI可删除
     * @author 101011000
     *
     */
    private static class DrawMap extends JComponent{
    	private int step = 20;
    	private int[][] map;
    	private AStarNode[][] nodes;
    	private List<List<Point>> pathlist;
    	public DrawMap(int[][] map,AStarNode[][] nodes,List<List<Point>> pathlist) {
    		this.map = map;
    		this.nodes = nodes;
    		this.pathlist = pathlist;
    		this.addMouseMotionListener(new MouseMotionListener() {

    			@Override
    			public void mouseDragged(MouseEvent e) {
    				// TODO Auto-generated method stub
    				int x = e.getX()/step;
    				int y = e.getY()/step;
    				String s = "("+x+","+y+")";
    				if(nodes[x][y]!=null)
    					s += " "+nodes[x][y].getFCost();
    				setToolTipText(s);
    			}

    			@Override
    			public void mouseMoved(MouseEvent e) {
    				int x = e.getX()/step;
    				int y = e.getY()/step;
    				String s = "("+x+","+y+")";
    				if(nodes[x][y]!=null)
    					s += " "+nodes[x][y].getFCost();
    				setToolTipText(s);
    			}
    			
    		});
    	}
    	
    	@Override
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
    		for(int i=0;i<map.length;i++) {
    			for(int j=0;j<map[0].length;j++) {
    				if(map[i][j]>0) g.setColor(Color.white);
    				else g.setColor(Color.gray);
    				if(nodes[i][j]!=null) g.setColor(Color.blue);
    				g.fillRect(step*i, step*j, step, step);
    				g.setColor(Color.black);
    				g.drawRect(step*i, step*j, step, step);
    			}
    		}
    		for(List<Point> path:pathlist) {
	    		Point pre = path.get(0);
	    		for(Point p:path) {
	    			g.setColor(Color.red);
	    			g.drawLine(pre.x*step+step/2, pre.y*step+step/2, p.x*step+step/2, p.y*step+step/2);
	    			pre = p;
	    		}
	    		g.fillOval(pre.x*step, pre.y*step, step, step);
    		}
    	}
    }
    /***************************************************************************************************/
}


