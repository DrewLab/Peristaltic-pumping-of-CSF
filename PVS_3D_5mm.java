/*
 * PVS_3D_5mm.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Scanner;
import java.nio.file.Paths;

/** Model exported on Mar 3 2020, 10:58 by COMSOL 5.4.0.388. */
// edited by Ravi Kedarasetti for readability and modularity

public class PVS_3D_5mm {
	public static Model setparams(Model model, String fileName) {
	  File file = new File(fileName);
	  try {
		Scanner sc = new Scanner(file);
	
		while (sc.hasNextLine()){
			String data = sc.nextLine();
			String[] splitData = data.split(" ", 0);
			model.param().set(splitData[0], splitData[1]);
			}
		}
	catch (Exception e) {
		System.out.println("Missing Parameters file :" + fileName);
	}
		return model;
	  
  }
  
  // read variables from files
  public static Model readVarData(Model model, String fileName, String varName) {
	File file = new File(fileName);
	try {
		Scanner sc = new Scanner(file);
		
		while (sc.hasNextLine()){
			String data = sc.nextLine();
			String[] splitData = data.split(" ", 0);
			model.component("comp1").variable(varName).set(splitData[0], splitData[1]);
			}
		}
	catch (Exception e) {
		System.out.println("Missing variable file :" + fileName + e);
	}
		return model;
  }
  
  // Read pulse waveform data from text file  
  
  public static Model readVesselData(Model model, String fileName, String funName) {
	File file = new File(fileName);
	try {
		
		Scanner sc1 = new Scanner(file); // dummy scanner to count lines
		int nLines = 0;
		while (sc1.hasNextLine()){
			nLines++;
			sc1.nextLine();
		}
		sc1.close();
		
		Scanner sc = new Scanner(file);
		String[][] dataLines = new String[nLines][2];
		
		int i = 0;
		while (sc.hasNextLine()){
			String data = sc.nextLine();
			String[] splitData = data.split(" ", 0);
			dataLines[i][0] = splitData[0];
			dataLines[i][1] = splitData[1];
			i++;
			}
		model.func().create(funName, "Interpolation");
		model.func(funName).set("table", dataLines);
		model.func(funName).set("interp", "cubicspline");
		}
	catch (Exception e) {
		System.out.println("Missing data file :" + fileName + e);
	}
		return model;
	  
  }
  public static Model run() {
    String modelPath = Paths.get(".").toAbsolutePath().normalize().toString();
    
    Model model = ModelUtil.create("Model");

    model.modelPath(modelPath);
	model.label("PVS_3D_5mm.mph");
	
	//create parameters
	String paramsFile = modelPath + "/Peristalsis_3D_5mm_params.txt";
	model = setparams(model, paramsFile); // set parameters from the paramFile

    //create functions
    model.func().create("step1", "Step");
    model.func().create("step2", "Step");
    model.func().create("an1", "Analytic");
    model.func("step1").set("location", 0.14);
    model.func("step1").set("smooth", 0.2);
    model.func("step2").set("location", 0.65);
    model.func("step2").set("from", 1);
    model.func("step2").set("to", "-wallfac");
    model.func("step2").set("smooth", 0.1);
    String dataFile = modelPath + "/HeartbeatWaveform.txt";
                                    
    model = readVesselData(model, dataFile, "int1");
    model.func("an1").set("expr", "int1(mod(x,1))");
    model.func("an1").set("plotargs", new String[][]{{"x", "0", "2"}});
    
    //create geometry and mesh
    model.component().create("comp1", true);
    model.component("comp1").geom().create("geom1", 3);
    model.result().table().create("evl3", "Table");
    model.result().table("evl3").label("Evaluation 3D");
    model.result().table("evl3").comments("Interactive 3D values");
    model.component("comp1").mesh().create("mesh1");

    model.component("comp1").geom("geom1").geomRep("comsol");
    model.component("comp1").geom("geom1").create("wp1", "WorkPlane");
    model.component("comp1").geom("geom1").feature("wp1").set("unite", true);
    model.component("comp1").geom("geom1").feature("wp1").geom().create("c1", "Circle");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("c1").set("r", "Leq*1.1");
    model.component("comp1").geom("geom1").feature("wp1").geom().create("e1", "Ellipse");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("e1")
         .set("semiaxes", new String[]{"(R1/Ro-0.1)*Leq", "0.7"});
    model.component("comp1").geom("geom1").feature("wp1").geom().create("dif1", "Difference");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("dif1").selection("input").set("e1");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("dif1").selection("input2").set("c1");
    model.component("comp1").geom("geom1").feature("wp1").geom().create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("r1").set("pos", new int[]{0, 0});
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("r1").set("size", new int[]{3, 2});
    model.component("comp1").geom("geom1").feature("wp1").geom().create("int1", "Intersection");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("int1").selection("input").set("dif1", "r1");
    model.component("comp1").geom("geom1").feature("wp1").geom().create("fil1", "Fillet");
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("fil1").set("radius", 0.06);
    model.component("comp1").geom("geom1").feature("wp1").geom().feature("fil1").selection("point").set("int1(1)", 1);
    model.component("comp1").geom("geom1").create("ext1", "Extrude");
    model.component("comp1").geom("geom1").feature("ext1").setIndex("distance", "10", 0);
    model.component("comp1").geom("geom1").feature("ext1").selection("input").set("wp1");
    model.component("comp1").geom("geom1").create("wp2", "WorkPlane");
    model.component("comp1").geom("geom1").feature("wp2").set("unite", true);
    model.component("comp1").geom("geom1").feature("wp2").geom().create("c1", "Circle");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("c1").set("r", "Leq");
    model.component("comp1").geom("geom1").feature("wp2").geom().create("e1", "Ellipse");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("e1")
         .set("semiaxes", new String[]{"R1/Ro*Leq", "0.8"});
    model.component("comp1").geom("geom1").feature("wp2").geom().create("dif1", "Difference");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("dif1").selection("input").set("e1");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("dif1").selection("input2").set("c1");
    model.component("comp1").geom("geom1").feature("wp2").geom().create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("r1").set("pos", new int[]{0, 0});
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("r1").set("size", new int[]{3, 2});
    model.component("comp1").geom("geom1").feature("wp2").geom().create("int1", "Intersection");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("int1").selection("input").set("dif1", "r1");
    model.component("comp1").geom("geom1").feature("wp2").geom().create("fil1", "Fillet");
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("fil1").set("radius", 0.08);
    model.component("comp1").geom("geom1").feature("wp2").geom().feature("fil1").selection("point").set("int1(1)", 1);
    model.component("comp1").geom("geom1").create("ext2", "Extrude");
    model.component("comp1").geom("geom1").feature("ext2").setIndex("distance", "10", 0);
    model.component("comp1").geom("geom1").feature("ext2").selection("input").set("wp2");
    model.component("comp1").geom("geom1").create("ls1", "LineSegment");
    model.component("comp1").geom("geom1").feature("ls1").selection("vertex1").set("ext1(1)", 2);
    model.component("comp1").geom("geom1").feature("ls1").selection("vertex2").set("ext2(1)", 2);
    model.component("comp1").geom("geom1").create("ls2", "LineSegment");
    model.component("comp1").geom("geom1").feature("ls2").selection("vertex1").set("ext2(1)", 4);
    model.component("comp1").geom("geom1").feature("ls2").selection("vertex2").set("ext1(1)", 4);
    model.component("comp1").geom("geom1").create("mir1", "Mirror");
    model.component("comp1").geom("geom1").feature("mir1").set("keep", true);
    model.component("comp1").geom("geom1").feature("mir1").set("axis", new int[]{0, 1, 0});
    model.component("comp1").geom("geom1").feature("mir1").selection("input").set("ext1", "ext2");
    model.component("comp1").geom("geom1").run();
    model.component("comp1").geom("geom1").run("wp2");

    
    //create variables
    model.component("comp1").variable().create("var1");
    String varFile1 = modelPath + "/NS-ALE-3DFormulationVariables.txt";
    model = readVarData(model, varFile1, "var1");
    
    model.component("comp1").variable().create("var2");
    String varFile2 = modelPath + "/Peristalsis_3D_BC.txt";
    model = readVarData(model, varFile2, "var2");
    model.component("comp1").variable("var1").label("Formulation");
    model.component("comp1").variable("var2").label("Boundaries");
    

    model.view().create("view4", 2);
    model.component("comp1").view("view1").set("renderwireframe", true);
    model.component("comp1").view("view1").set("scenelight", false);
    model.component("comp1").view("view2").axis().set("xmin", -2.2292795181274414);
    model.component("comp1").view("view2").axis().set("xmax", 5.334416389465332);
    model.component("comp1").view("view2").axis().set("ymin", -0.7808437347412109);
    model.component("comp1").view("view2").axis().set("ymax", 0.7808437347412109);
    model.component("comp1").view("view3").axis().set("xmin", -0.3392859101295471);
    model.component("comp1").view("view3").axis().set("xmax", 3.444362163543701);
    model.component("comp1").view("view3").axis().set("ymin", -0.01860029622912407);
    model.component("comp1").view("view3").axis().set("ymax", 0.7626150846481323);
    model.view("view4").axis().set("xmin", -3.653198719024658);
    model.view("view4").axis().set("xmax", 3.653198719024658);
    
	
	// Area for outflow
    model.component("comp1").cpl().create("intop1", "Integration");
    model.component("comp1").cpl("intop1").selection().geom("geom1", 2);
    model.component("comp1").cpl("intop1").selection().set(4, 8, 9, 12, 16, 20);
    model.component("comp1").cpl("intop1").label("Outflow");
    model.component("comp1").cpl("intop1").set("opname", "Outlet");
    
    // perimeter for Reynolds and Peclet number calculations
    model.component("comp1").cpl().create("intop2", "Integration");
    model.component("comp1").cpl("intop2").selection().geom("geom1", 1);
    model.component("comp1").cpl("intop2").selection().set(4, 5, 9, 11, 14, 18);
    model.component("comp1").cpl("intop2").label("Perimeter");
    model.component("comp1").cpl("intop2").set("opname", "perim");


	//mesh displacement
    model.component("comp1").physics().create("umeq", "WeakFormPDE", "geom1");
    model.component("comp1").physics("umeq").identifier("umeq");
    model.component("comp1").physics("umeq").field("dimensionless").field("um");
    model.component("comp1").physics("umeq").field("dimensionless").component(new String[]{"umx", "umy", "umz"});
    model.component("comp1").physics("umeq").create("dir1", "DirichletBoundary", 2);
    model.component("comp1").physics("umeq").feature("dir1").selection().set(2, 6);
    model.component("comp1").physics("umeq").create("dir2", "DirichletBoundary", 2);
    model.component("comp1").physics("umeq").feature("dir2").selection().set(10, 11);
    model.component("comp1").physics("umeq").create("dir3", "DirichletBoundary", 2);
    model.component("comp1").physics("umeq").feature("dir3").selection().set(1);
    model.component("comp1").physics("umeq").create("dir4", "DirichletBoundary", 2);
    model.component("comp1").physics("umeq").feature("dir4").selection().set(5);
    
    model.component("comp1").physics("umeq").label("Mesh displacement");
    model.component("comp1").physics("umeq").feature("wfeq1")
         .set("weak", new String[][]{{"umWC/Leq^3"}, {"0"}, {"0"}});
    model.component("comp1").physics("umeq").feature("dir1")
         .set("r", new String[][]{{"urad*x/r"}, {"urad*y/r"}, {"0"}});
    model.component("comp1").physics("umeq").feature("dir1").label("Peristaltic Wave");
    model.component("comp1").physics("umeq").feature("dir2")
         .set("r", new String[][]{{"urad*nx*wallfac"}, {"urad*ny*wallfac"}, {"0"}});
    model.component("comp1").physics("umeq").feature("dir2").label("Fixed Wall");
    model.component("comp1").physics("umeq").feature("dir3")
         .set("r", new String[][]{{"-urad*step2(-y/Leq)*nx"}, {"-urad*step2(-y/Leq)*ny"}, {"0"}});
    model.component("comp1").physics("umeq").feature("dir3").label("Transition 1");
    model.component("comp1").physics("umeq").feature("dir4")
         .set("r", new String[][]{{"-urad*step2(y/Leq)*nx"}, {"-urad*step2(y/Leq)*ny"}, {"0"}});
    model.component("comp1").physics("umeq").feature("dir4").label("Transition 2");
         
    // fluid velocity
    model.component("comp1").physics().create("vfeq", "WeakFormPDE", "geom1");
    model.component("comp1").physics("vfeq").identifier("vfeq");
    model.component("comp1").physics("vfeq").field("dimensionless").field("vf");
    model.component("comp1").physics("vfeq").field("dimensionless").component(new String[]{"vfx", "vfy", "vfz"});
    model.component("comp1").physics("vfeq").create("dir1", "DirichletBoundary", 2);
    model.component("comp1").physics("vfeq").feature("dir1").selection().set(2, 6);
    model.component("comp1").physics("vfeq").create("dir2", "DirichletBoundary", 2);
    model.component("comp1").physics("vfeq").feature("dir2").selection().set(10, 11);
    model.component("comp1").physics("vfeq").create("dir3", "DirichletBoundary", 2);
    model.component("comp1").physics("vfeq").feature("dir3").selection().set(1);
    model.component("comp1").physics("vfeq").create("dir4", "DirichletBoundary", 2);
    model.component("comp1").physics("vfeq").feature("dir4").selection().set(5);
    
    model.component("comp1").physics("vfeq").label("Fluid Velocity");
    model.component("comp1").physics("vfeq").feature("wfeq1")
         .set("weak", new String[][]{{"vfWC/Leq^3"}, {"0"}, {"0"}});
    model.component("comp1").physics("vfeq").feature("dir1")
         .set("r", new String[][]{{"vrad*x/r"}, {"vrad*y/r"}, {"0"}});
    model.component("comp1").physics("vfeq").feature("dir1").label("Peristaltic Wave");
    model.component("comp1").physics("vfeq").feature("dir2")
         .set("r", new String[][]{{"vrad*nx*wallfac"}, {"vrad*ny*wallfac"}, {"0"}});
    model.component("comp1").physics("vfeq").feature("dir2").label("Fixed Wall");
    model.component("comp1").physics("vfeq").feature("dir3")
         .set("r", new String[][]{{"-vrad*step2(-y/Leq)*nx"}, {"-vrad*step2(-y/Leq)*ny"}, {"0"}});
    model.component("comp1").physics("vfeq").feature("dir3").label("Transition 1");
    model.component("comp1").physics("vfeq").feature("dir4")
         .set("r", new String[][]{{"-vrad*step2(y/Leq)*nx"}, {"-vrad*step2(y/Leq)*ny"}, {"0"}});
    model.component("comp1").physics("vfeq").feature("dir4").label("Transition 2");
    
    //fluid pressure
    model.component("comp1").physics().create("pfeq", "WeakFormPDE", "geom1");
    model.component("comp1").physics("pfeq").identifier("pfeq");
    model.component("comp1").physics("pfeq").field("dimensionless").field("pf");
    model.component("comp1").physics("pfeq").field("dimensionless").component(new String[]{"pf"});
	
	model.component("comp1").physics("pfeq").label("Pressure");
    model.component("comp1").physics("pfeq").prop("ShapeProperty").set("order", 1);
    model.component("comp1").physics("pfeq").feature("wfeq1").set("weak", "pfWC/Leq^3");
	
	//Build mesh
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").create("fq2", "FreeQuad");
    model.component("comp1").mesh("mesh1").create("map2", "Map");
    model.component("comp1").mesh("mesh1").create("swe1", "Sweep");
    model.component("comp1").mesh("mesh1").create("cpd1", "CopyDomain");
    model.component("comp1").mesh("mesh1").create("cpd2", "CopyDomain");
    model.component("comp1").mesh("mesh1").feature("map1").selection().set(8, 9, 12);
    model.component("comp1").mesh("mesh1").feature("map1").create("dis1", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis2", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis6", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis3", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis4", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis5", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis7", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis8", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").selection().set(4, 9, 22, 27);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").selection().set(5, 11);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis6").selection().set(23, 28);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis3").selection().set(17);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis4").selection().set(10, 31);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis5").selection().set(18);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis7").selection().set(34, 37);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis8").selection().set(43);
    model.component("comp1").mesh("mesh1").feature("fq2").selection().set(20);
    model.component("comp1").mesh("mesh1").feature("fq2").create("size1", "Size");
    model.component("comp1").mesh("mesh1").feature("fq2").create("dis1", "Distribution");
    model.component("comp1").mesh("mesh1").feature("fq2").feature("dis1").selection().set(40);
    model.component("comp1").mesh("mesh1").feature("map2").selection().set(5, 6, 11, 21, 24, 25);
    model.component("comp1").mesh("mesh1").feature("map2").create("size1", "Size");
    model.component("comp1").mesh("mesh1").feature("swe1").selection().geom("geom1", 3);
    model.component("comp1").mesh("mesh1").feature("swe1").selection().set(2, 4);

    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("numelem", 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("reverse", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("elemcount", 6);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis6").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis6").set("reverse", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis6").set("elemcount", 6);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis6").set("elemratio", 2.5);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis3").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis3").set("elemcount", 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis3").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis4").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis4").set("reverse", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis4").set("elemcount", 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis4").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis5").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis5").set("elemcount", 14);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis5").set("elemratio", 1.5);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis7").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis7").set("reverse", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis7").set("elemcount", 14);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis7").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis8").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis8").set("elemcount", 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis8").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("fq2").set("method", "legacy52a");
    model.component("comp1").mesh("mesh1").feature("fq2").feature("size1").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("fq2").feature("size1").set("hmax", 0.1);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("size1").set("hmaxactive", true);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("size1").set("hgrad", 1.3);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("size1").set("hgradactive", true);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("dis1").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("fq2").feature("dis1").set("elemcount", 11);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("dis1").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("fq2").feature("dis1").set("symmetric", true);
    model.component("comp1").mesh("mesh1").feature("map2").feature("size1").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("map2").feature("size1").set("hmax", 0.25);
    model.component("comp1").mesh("mesh1").feature("map2").feature("size1").set("hmaxactive", true);
    model.component("comp1").mesh("mesh1").feature("swe1").selection("sourceface").set(8, 9, 12);
    model.component("comp1").mesh("mesh1").feature("swe1").selection("targetface").set(7, 19);
    model.component("comp1").mesh("mesh1").feature("cpd1").selection("source").set(2);
    model.component("comp1").mesh("mesh1").feature("cpd1").selection("destination").set(1);
    model.component("comp1").mesh("mesh1").feature("cpd2").selection("source").set(4);
    model.component("comp1").mesh("mesh1").feature("cpd2").selection("destination").set(3);
    model.component("comp1").mesh("mesh1").run();

    //create study
    model.study().create("std1");
    model.study("std1").create("time", "Transient");
    model.study("std1").feature("time").set("useadvanceddisable", true);
    model.study("std1").feature("time").set("activate", new String[]{"umeq", "on", "vfeq", "on", "pfeq", "on"});

    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature().remove("fcDef");

    model.result().dataset().create("tavg1", "TimeAverage");
    model.result().dataset().create("cpt1", "CutPoint3D");
    model.result().dataset().create("cpt2", "CutPoint3D");
    model.result().dataset().create("an1_ds1", "Grid1D");
    model.result().dataset("an1_ds1").set("data", "none");
    model.result().create("pg2", "PlotGroup3D");
    model.result().create("pg3", "PlotGroup3D");
    model.result().create("pg8", "PlotGroup1D");
    model.result().create("pg12", "PlotGroup1D");
    model.result().create("pg13", "PlotGroup3D");
    model.result("pg2").create("slc1", "Slice");
    model.result("pg2").create("arwv1", "ArrowVolume");
    model.result("pg2").feature("slc1").create("def1", "Deform");
    model.result("pg3").create("slc1", "Slice");
    model.result("pg3").feature("slc1").create("def1", "Deform");
    model.result("pg8").create("ptgr1", "PointGraph");
    model.result("pg8").create("ptgr2", "PointGraph");
    model.result("pg12").create("glob1", "Global");
    model.result("pg13").create("slc1", "Slice");
    model.result("pg13").feature("slc1").create("def1", "Deform");
    model.result().export().create("data1", "Data");

    model.study("std1").feature("time").set("tlist", "range(0,0.1,3.9) range(4,0.01,5)");
    model.study("std1").feature("time")
         .set("discretization", new String[]{"umeq", "physics", "vfeq", "physics", "pfeq", "physics"});

    model.sol("sol1").attach("std1");
    model.sol("sol1").feature("v1").set("resscalemethod", "auto");
    model.sol("sol1").feature("v1").set("clist", new String[]{"range(0,0.1,3.9) range(4,0.01,5)", "0.001[s]"});
    model.sol("sol1").feature("t1").set("tlist", "range(0,0.1,3.9) range(4,0.01,5)");
    model.sol("sol1").feature("t1").set("tstepsbdf", "intermediate");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", 0.001);
    model.sol("sol1").feature("t1").set("consistent", false);
    model.sol("sol1").feature("t1").set("estrat", "exclude");
    model.sol("sol1").feature("t1").feature("fc1").active(true);
    model.sol("sol1").feature("t1").feature("fc1").set("stabacc", "aacc");
    model.sol("sol1").feature("t1").feature("fc1").set("aaccdim", 25);
    // model.sol("sol1").runFromTo("st1", "v1"); // to initialize the model
    model.sol("sol1").runAll();

    model.result().dataset("tavg1").set("looplevelinput", new String[]{"manualindices"});
    model.result().dataset("tavg1").set("looplevelindices", new String[]{"range(41,1,141)"});
    model.result().dataset("cpt1").set("pointx", "1.5*Leq");
    model.result().dataset("cpt1").set("pointy", 0);
    model.result().dataset("cpt1").set("pointz", "(La-1[mm])*Leq*10/La");
    model.result().dataset("cpt2").set("pointx", "Leq");
    model.result().dataset("cpt2").set("pointy", 0);
    model.result().dataset("cpt2").set("pointz", "(La-1[mm])*Leq*10/La");
    model.result().dataset("cpt2").set("pointvar", "cpt1n");
    model.result().dataset("an1_ds1").set("function", "all");
    model.result().dataset("an1_ds1").set("parmax1", 2);
    model.result().dataset("an1_ds1").set("res1", 10000);
    model.result().dataset("an1_ds1").set("adaptive", false);
    model.result("pg2").label("Axial velocity");
    model.result("pg2").feature("slc1").set("expr", "vfz*vo");
    model.result("pg2").feature("slc1").set("unit", "\u00b5m/s");
    model.result("pg2").feature("slc1").set("descr", "vfz*vo");
    model.result("pg2").feature("slc1").set("quickplane", "zx");
    model.result("pg2").feature("slc1").set("quickynumber", 1);
    model.result("pg2").feature("slc1").set("rangecoloractive", true);
    model.result("pg2").feature("slc1").set("rangecolormin", -15000);
    model.result("pg2").feature("slc1").set("rangecolormax", 15000);
    model.result("pg2").feature("slc1").set("colortable", "Twilight");
    model.result("pg2").feature("slc1").set("colortablerev", true);
    model.result("pg2").feature("slc1").set("resolution", "norefine");
    model.result("pg2").feature("slc1").set("smooth", "none");
    model.result("pg2").feature("slc1").set("resolution", "norefine");
    model.result("pg2").feature("slc1").feature("def1")
         .set("expr", new String[]{"umx*uo*Leq/Lo/g1", "umy*uo*Leq/Lo/g2", "umz*uo*Leq/Lo/g3"});
    model.result("pg2").feature("slc1").feature("def1").set("scale", 50);
    model.result("pg2").feature("slc1").feature("def1").set("scaleactive", true);
    model.result("pg2").feature("arwv1").set("expr", new String[]{"vfx", "vfy", "vfz"});
    model.result("pg2").feature("arwv1").set("descr", "");
    model.result("pg2").feature("arwv1").set("ynumber", 1);
    model.result("pg2").feature("arwv1").set("znumber", 20);
    model.result("pg2").feature("arwv1").set("scale", 0.03);
    model.result("pg2").feature("arwv1").set("scaleactive", true);
    model.result("pg2").feature("arwv1").set("color", "black");
    model.result("pg3").label("Pressure");
    model.result("pg3").feature("slc1").set("expr", "pf*po");
    model.result("pg3").feature("slc1").set("unit", "Torr");
    model.result("pg3").feature("slc1").set("descr", "pf*po");
    model.result("pg3").feature("slc1").set("quickplane", "zx");
    model.result("pg3").feature("slc1").set("quickynumber", 1);
    model.result("pg3").feature("slc1").set("rangecoloractive", true);
    model.result("pg3").feature("slc1").set("rangecolormin", -40);
    model.result("pg3").feature("slc1").set("rangecolormax", 40);
    model.result("pg3").feature("slc1").set("colortable", "HeatCameraLight");
    model.result("pg3").feature("slc1").set("colortablerev", true);
    model.result("pg3").feature("slc1").set("smooth", "internal");
    model.result("pg3").feature("slc1").set("resolution", "normal");
    model.result("pg3").feature("slc1").feature("def1")
         .set("expr", new String[]{"umx*uo*Leq/Lo/g1", "umy*uo*Leq/Lo/g2", "umz*uo*Leq/Lo/g3"});
    model.result("pg3").feature("slc1").feature("def1").set("scale", 50);
    model.result("pg3").feature("slc1").feature("def1").set("scaleactive", true);
    model.result("pg8").label("Particle and Wall velocity");
    model.result("pg8").set("xlabel", "Time (s)");
    model.result("pg8").set("ylabel", "Fluid velocity (m/s)");
    model.result("pg8").set("yseclabel", "Wall velocity (m/s)");
    model.result("pg8").set("twoyaxes", true);
    model.result("pg8")
         .set("plotonsecyaxis", new String[][]{{"Point Graph 1", "off", "ptgr1"}, {"Point Graph 2", "on", "ptgr2"}});
    model.result("pg8").set("xlabelactive", false);
    model.result("pg8").set("ylabelactive", false);
    model.result("pg8").set("yseclabelactive", false);
    model.result("pg8").feature("ptgr1").set("data", "cpt1");
    model.result("pg8").feature("ptgr1").set("looplevelinput", new String[]{"manualindices"});
    model.result("pg8").feature("ptgr1").set("looplevelindices", new String[]{"range(42,1,141)"});
    model.result("pg8").feature("ptgr1").set("expr", "vfz*vo");
    model.result("pg8").feature("ptgr1").set("unit", "m/s");
    model.result("pg8").feature("ptgr1").set("descractive", true);
    model.result("pg8").feature("ptgr1").set("descr", "Fluid velocity");
    model.result("pg8").feature("ptgr2").set("data", "cpt2");
    model.result("pg8").feature("ptgr2").set("looplevelinput", new String[]{"manualindices"});
    model.result("pg8").feature("ptgr2").set("looplevelindices", new String[]{"range(42,1,141)"});
    model.result("pg8").feature("ptgr2").set("expr", "umxt*uo/tau*teq");
    model.result("pg8").feature("ptgr2").set("unit", "m/s");
    model.result("pg8").feature("ptgr2").set("descractive", true);
    model.result("pg8").feature("ptgr2").set("descr", "Wall velocity");
    model.result("pg12").label("Reynold number");
    model.result("pg12").set("looplevelinput", new String[]{"manualindices"});
    model.result("pg12").set("looplevelindices", new String[]{"range(41,1,141)"});
    model.result("pg12").set("xlabel", "Time (s)");
    model.result("pg12").set("xlabelactive", false);
    model.result("pg12").feature("glob1").set("expr", new String[]{"rhof*Outlet(flow)*4*Leq/perim(1)/Lo/muf"});
    model.result("pg12").feature("glob1").set("unit", new String[]{"1"});
    model.result("pg12").feature("glob1").set("descr", new String[]{""});
    model.result("pg13").label("Peclet Number");
    model.result("pg13").set("data", "tavg1");
    model.result("pg13").set("solrepresentation", "solnum");
    model.result("pg13").feature("slc1").set("expr", "vfz*vo*4*Lo*Outlet(1)/perim(1)/Leq/D");
    model.result("pg13").feature("slc1").set("descr", "vfz*vo*4*Lo*Outlet(1)/perim(1)/Leq/D");
    model.result("pg13").feature("slc1").set("quickplane", "zx");
    model.result("pg13").feature("slc1").set("quickynumber", 1);
    model.result("pg13").feature("slc1").set("smooth", "internal");
    model.result("pg13").feature("slc1").set("resolution", "normal");
    model.result("pg13").feature("slc1").feature("def1")
         .set("expr", new String[]{"umx*uo*Leq/Lo/g1", "umy*uo*Leq/Lo/g2", "umz*uo*Leq/Lo/g3"});
    model.result("pg13").feature("slc1").feature("def1").set("scale", 10);
    model.result("pg13").feature("slc1").feature("def1").set("scaleactive", true);
    
    // export data
    model.result().export("data1").set("looplevelinput", new String[]{"manualindices"});
    model.result().export("data1").set("looplevelindices", new String[]{"range(41,1,140)"});
    model.result().export("data1").set("expr", new String[]{"umx", "umy", "umz", "xcdotx", "xcdoty", "xcdotz"});
    model.result().export("data1").set("unit", new String[]{"1", "1", "1", "m/s", "m/s", "m/s"});
    model.result().export("data1").set("descr", new String[]{"", "", "", "", "", ""});
    model.result().export("data1").set("filename", modelPath + "/pvs5mmResults.txt");
    model.result().export("data1").set("location", "grid");
    model.result().export("data1").set("gridx3", "range(1,0.05,2)");
    model.result().export("data1").setIndex("gridy3", "0", 0);
    model.result().export("data1").set("gridz3", "range(0,0.1,10)");
    model.result().export("data1").set("header", false);
	model.result().export("data1").run();
	
    return model;
  }

  public static void main(String[] args) {
    Model model = run();
  }

}
