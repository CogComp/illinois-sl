package edu.illinois.cs.cogcomp.sl.bilinear;

import algebra.Prob;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.DenseVector;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Shyam on 12/2/15.
 */
public class Utils {
    public static void load_matrix(String matrix, DenseVector[] target) {
        try (BufferedReader br = new BufferedReader(new FileReader(matrix))) {
            String line;
            int row=0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                int col=0;
                target[row]=new DenseVector();
                for(String part:parts)
                {
                    target[row].setElement(col++,Float.parseFloat(part));
                }
                row++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void save_matrix(String matrix, DenseVector[] target){
        try {
            PrintWriter w = new PrintWriter(matrix);

            for(int i=0;i<target.length;i++)
            {
                for(int j=0;j<target[i].getLength();j++)
                {
                    w.print(target[i].get(j)+" ");
                }
                w.println();
            }
            w.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void renormalize(DenseVector[] u) {
        System.out.println("renormalizing ...");
        for(int i=0;i<u.length;i++)
        {
            double invSqrt = 1.0 / Math.sqrt(u[i].getSquareL2Norm());
            u[i].scale(invSqrt);
        }
    }




    public static SLProblem readTrainingData(String allDataPath,String trainingDataPath,Map<Integer,String>denum, int limit) throws FileNotFoundException {
        List<String> train = LineIO.read(trainingDataPath);

        Gson g = new Gson();
        String s = LineIO
                .slurp(allDataPath);
        Type listType = new TypeToken<ArrayList<Prob>>() {
        }.getType();
        List<Prob> coll2 = g.fromJson(s, listType);
        SLProblem data = new SLProblem();
        for(Prob coll:coll2)
        {
            Collections.sort(coll.Template);
            String sign =String.join("#", coll.Template);

            if(train.contains(coll.iIndex+"")) {
                String denumQ = denum.get(coll.iIndex);
                AlgebraInstance iinst = new AlgebraInstance(coll.iIndex,coll.sQuestion,denumQ,sign);
                int id = MainClass.getLabelId(sign);

                if(id>=0)
                {
                    TemplateLabel istruct = new TemplateLabel(sign, id);
                    data.addExample(iinst, istruct);
                }
            }
            if(data.size()==limit)
                break;
        }
        System.out.println("data size:"+data.size());
        return data;
    }


    public static SLProblem readNoisyData(String noisyPath,Map<Integer,String>denum, int limit) throws FileNotFoundException {
        Gson g = new Gson();
        String s = LineIO
                .slurp(noisyPath);
        Type listType = new TypeToken<ArrayList<Prob>>() {
        }.getType();
        List<Prob> coll2 = g.fromJson(s, listType);
        SLProblem data = new SLProblem();
        for(Prob coll:coll2)
        {
            Collections.sort(coll.Template);
            String sign =String.join("#", coll.Template);
            String denumQ = denum.get(coll.iIndex);
            AlgebraInstance iinst = new AlgebraInstance(coll.iIndex,coll.sQuestion,denumQ,sign);
            int id = MainClass.getLabelId(sign);

            if(id>=0)
            {
                TemplateLabel istruct = new TemplateLabel(sign, id);
                data.addExample(iinst, istruct);
            }
            else
            {
                System.err.println("This cannot be "+sign);
                System.exit(-1);
            }
            if(data.size()==limit)
                break;
        }
        System.out.println("noisy data size "+data.size());
        return data;
    }

}
