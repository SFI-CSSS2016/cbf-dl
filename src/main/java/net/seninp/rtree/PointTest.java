package net.seninp.rtree;

import java.util.List;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

public class PointTest {

  public static void main(String[] args) {

    // TODO Auto-generated method stub
    RTree<String, Geometry> tree = RTree.create();
    tree = tree.add("1", Geometries.point(100, 100));
    tree = tree.add("2", Geometries.point(0, 0));
    tree = tree.add("3", Geometries.point(50, 50));

    System.out.println(tree.asString());

    tree.visualize(600, 600).save("target/mytree.png");

   List<Entry<String, Geometry>> res1 = tree.search(Geometries.point(0.45, 0.45), 10).toList().toBlocking().single();

    System.out.println(res1.get(0));
  }

}
