import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Scanner;
import java.nio.file.Paths;

/** Model exported on May 6 2020, 18:53 by COMSOL 5.4.0.388. */
// Modified by Ravi Kedarasetti for readability and modularity
public class Mesh_convergence_AS {
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
  
  public static Model run() {
    String modelPath = Paths.get(".").toAbsolutePath().normalize().toString();
    
    Model model = ModelUtil.create("Model");
    model.label("Mesh_convergence_AS.mph");
    
    //read parameters
	String paramsFile = modelPath + "/Peristalsis_as_params.txt";
	model = setparams(model, paramsFile); // set parameters from the paramFile
        
	//create functions
    model.func().create("step1", "Step");
    model.func("step1").set("location", 0.07);

	//create component with axisymmetric geometry
    model.component().create("comp1", true);
	model.component("comp1").geom().create("geom1", 2);
    model.component("comp1").geom("geom1").axisymmetric(true);
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("pos", new String[]{"Leq", "0"});
    model.component("comp1").geom("geom1").feature("r1").set("size", new String[]{"(R2/R1-1)*Leq", "Leq*g3fac"});
    model.component("comp1").geom("geom1").run();

	model.component("comp1").mesh().create("mesh1");
    
    // create variables
    model.component("comp1").variable().create("var1");
    String varFile1 = modelPath + "/NS-ALE-ASFormulationVariables.txt";
    model = readVarData(model, varFile1, "var1");
    
    model.component("comp1").variable().create("var2");
    String varFile2 = modelPath + "/Peristalsis_as_BC.txt";
    model = readVarData(model, varFile2, "var2");
    
    model.component("comp1").variable().create("var3");
    String varFile3 = modelPath + "/Mesh_sweep_vars.txt";
    model = readVarData(model, varFile3, "var3");
    
    model.component("comp1").variable("var1").label("Formulation");
    model.component("comp1").variable("var2").label("Boundaries");
    model.component("comp1").variable("var3").label("Error Norms");

    model.view().create("view2", 3);
    model.view().create("view3", 3);

    model.component("comp1").cpl().create("intop1", "Integration");
    model.component("comp1").cpl().create("intop2", "Integration");
    
    //Whole domain integration
    model.component("comp1").cpl("intop1").selection().all();
    model.component("comp1").cpl("intop1").set("opname", "DInt");
    model.component("comp1").cpl("intop1").set("axisym", true);
    
    // Outflow integration
    model.component("comp1").cpl("intop2").selection().geom("geom1", 1);
    model.component("comp1").cpl("intop2").selection().set(3);
    model.component("comp1").cpl("intop2").set("opname", "Outflow");
    model.component("comp1").cpl("intop2").set("axisym", true);

    //Mesh displacement
    model.component("comp1").physics().create("w", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w").field("dimensionless").field("um");
    model.component("comp1").physics("w").field("dimensionless").component(new String[]{"umr", "umz"});
    model.component("comp1").physics("w").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir1").selection().set(4);
    model.component("comp1").physics("w").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir2").selection().set(1);
    model.component("comp1").physics("w").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w").feature("pc1").selection().set(2, 3);
    
    model.component("comp1").physics("w").feature("wfeq1").set("weak", new String[][]{{"umWC*2*pi*r/Leq^3"}, {"0"}});
    model.component("comp1").physics("w").feature("dir1").label("Brain tissue");
    model.component("comp1").physics("w").feature("dir2").set("r", new String[][]{{"urad"}, {"0"}});
    model.component("comp1").physics("w").feature("dir2").label("Peristaltic wave");
    model.component("comp1").physics("w").active(false);
    
    // fluid velocity
    model.component("comp1").physics().create("w2", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w2").field("dimensionless").field("vf");
    model.component("comp1").physics("w2").field("dimensionless").component(new String[]{"vfr", "vfz"});
    model.component("comp1").physics("w2").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir1").selection().set(4);
    model.component("comp1").physics("w2").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir2").selection().set(1);
    model.component("comp1").physics("w2").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w2").feature("pc1").selection().set(2, 3);
    
    model.component("comp1").physics("w2").feature("wfeq1").set("weak", new String[][]{{"vfWC*2*pi*r/Leq^3"}, {"0"}});
    model.component("comp1").physics("w2").feature("dir1").label("Brain tissue");
    model.component("comp1").physics("w2").feature("dir2").set("r", new String[][]{{"vrad"}, {"0"}});
    model.component("comp1").physics("w2").feature("dir2").label("Peristaltic wave");
    
    //fluid pressure
    model.component("comp1").physics().create("w3", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w3").field("dimensionless").field("pf");
    model.component("comp1").physics("w3").field("dimensionless").component(new String[]{"pf"});
    model.component("comp1").physics("w3").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w3").feature("pc1").selection().set(2, 3);
    model.component("comp1").physics("w3").create("gconstr1", "GlobalConstraint", -1);
	
	model.component("comp1").physics("w3").prop("ShapeProperty").set("order", 1);
    model.component("comp1").physics("w3").feature("wfeq1").set("weak", "pfWC*2*pi*r");
    model.component("comp1").physics("w3").feature("gconstr1").set("constraintExpression", "DInt(pf)");

	// Mesh
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis1", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis2", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").selection().set(2, 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").selection().set(4);
	model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemcount", "Mh*5");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemratio", 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("symmetric", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("numelem", "Mh*50");
    model.component("comp1").mesh("mesh1").run();
    
    model.component("comp1").view("view1").axis().set("xmin", -1.1131505966186523);
    model.component("comp1").view("view1").axis().set("xmax", 6.341667652130127);
    model.component("comp1").view("view1").axis().set("ymin", 7.925600528717041);
    model.component("comp1").view("view1").axis().set("ymax", 11.298491477966309);
    model.view("view2").set("showgrid", false);



   

    model.study().create("std1");
    model.study("std1").create("param", "Parametric");
    model.study("std1").create("time", "Transient");

    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    model.sol().create("sol2");
    model.sol("sol2").study("std1");
    model.sol("sol2").label("Parametric Solutions 1");

    model.batch().create("p1", "Parametric");
    model.batch("p1").create("so1", "Solutionseq");
    model.batch("p1").study("std1");

    model.result().dataset().create("rev1", "Revolve2D");
    model.result().create("pg2", "PlotGroup3D");
    model.result().create("pg8", "PlotGroup1D");
    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").create("surf2", "Surface");
    model.result("pg2").feature("surf1").create("def1", "Deform");
    model.result("pg2").feature("surf2").create("def1", "Deform");
    model.result("pg8").set("data", "dset2");
    model.result("pg8").create("glob1", "Global");

    model.study("std1").feature("param").set("pname", new String[]{"Mh"});
    model.study("std1").feature("param").set("plistarr", new String[]{"2^range(0,1,4)"});
    model.study("std1").feature("param").set("punit", new String[]{""});
    model.study("std1").feature("time").set("tlist", "0,range(3,0.1,4)");
    model.study("std1").feature("time").set("useinitsol", true);

    model.sol("sol1").attach("std1");
    model.sol("sol1").feature("v1").set("clist", new String[]{"0,range(3,0.1,4)", "0.01/Mt[s]"});
    model.sol("sol1").feature("t1").set("tlist", "0,range(3,0.1,4)");
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.002/Mt");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.002/Mt");
    model.sol("sol1").feature("t1").set("consistent", false);
    model.sol("sol1").feature("t1").set("estrat", "exclude");

    model.batch("p1").set("control", "param");
    model.batch("p1").set("pname", new String[]{"Mh"});
    model.batch("p1").set("plistarr", new String[]{"2^range(0,1,4)"});
    model.batch("p1").set("punit", new String[]{""});
    model.batch("p1").set("err", true);
    model.batch("p1").feature("so1").set("seq", "sol1");
    model.batch("p1").feature("so1").set("psol", "sol2");
    model.batch("p1").feature("so1")
         .set("param", new String[]{"\"Mh\",\"1\"", "\"Mh\",\"2\"", "\"Mh\",\"4\"", "\"Mh\",\"8\"", "\"Mh\",\"16\""});
    model.batch("p1").attach("std1");
    model.batch("p1").run();

    model.result().dataset("rev1").set("startangle", -90);
    model.result().dataset("rev1").set("revangle", 225);
    model.result("pg2").label("Geometry");
    model.result("pg2").set("looplevel", new int[]{1});
    model.result("pg2").set("titletype", "none");
    model.result("pg2").set("edges", false);
    model.result("pg2").feature("surf1").set("expr", "step1(r/Leq-1)");
    model.result("pg2").feature("surf1").set("unit", "");
    model.result("pg2").feature("surf1").set("descr", "Step 1");
    model.result("pg2").feature("surf1").set("rangecoloractive", true);
    model.result("pg2").feature("surf1").set("rangecolormin", -0.1);
    model.result("pg2").feature("surf1").set("rangecolormax", 1.5);
    model.result("pg2").feature("surf1").set("colortable", "Disco");
    model.result("pg2").feature("surf1").set("colorlegend", false);
    model.result("pg2").feature("surf1").set("colortablerev", true);
    model.result("pg2").feature("surf1").set("resolution", "normal");
    model.result("pg2").feature("surf1").feature("def1").set("revcoordsys", "cylindrical");
    model.result("pg2").feature("surf1").feature("def1").set("expr", new String[]{"umr", "", "umz"});
    model.result("pg2").feature("surf1").feature("def1").set("scale", 0.3);
    model.result("pg2").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg2").feature("surf2").set("expr", "step1(r/Leq-R2/R1+0.1)");
    model.result("pg2").feature("surf2").set("unit", "");
    model.result("pg2").feature("surf2").set("descr", "Step 1");
    model.result("pg2").feature("surf2").set("rangecoloractive", true);
    model.result("pg2").feature("surf2").set("rangecolormin", -0.7);
    model.result("pg2").feature("surf2").set("rangecolormax", 1.5);
    model.result("pg2").feature("surf2").set("colortable", "Disco");
    model.result("pg2").feature("surf2").set("colorlegend", false);
    model.result("pg2").feature("surf2").set("resolution", "normal");
    model.result("pg2").feature("surf2").feature("def1").set("revcoordsys", "cylindrical");
    model.result("pg2").feature("surf2").feature("def1").set("expr", new String[]{"umr+0.2", "", "umz"});
    model.result("pg2").feature("surf2").feature("def1").set("scale", 0.3);
    model.result("pg2").feature("surf2").feature("def1").set("scaleactive", true);
    model.result("pg8").set("looplevelinput", new String[]{"last", "manual"});
    model.result("pg8").set("looplevel", new String[]{"1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12", "1, 2, 3, 4"});
    model.result("pg8").set("xlabel", "Mh");
    model.result("pg8").set("ylabel", "L2Err");
    model.result("pg8").set("ylabelactive", true);
    model.result("pg8").set("xlog", true);
    model.result("pg8").set("ylog", true);
    model.result("pg8").set("xlabelactive", false);
    model.result("pg8").feature("glob1").set("expr", new String[]{"L2ErrV", "L2ErrP"});
    model.result("pg8").feature("glob1").set("unit", new String[]{"1", "1"});
    model.result("pg8").feature("glob1").set("descr", new String[]{"v<sub>f</sub>", "p<sub>f</sub>"});
    model.result("pg8").feature("glob1").set("xdatasolnumtype", "level2");
    model.result("pg8").feature("glob1").set("linewidth", 2);
    model.result("pg8").feature("glob1").set("linemarker", "cyclereset");
    model.result("pg8").feature("glob1").set("markerpos", "datapoints");

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
