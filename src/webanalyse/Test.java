/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webanalyse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;



/**
 *
 * @author wiwat
 */
public class Test {
    
    public static void regex(List<String> args){
        Pattern pat = Pattern.compile(args.get(0));
        String subj = args.get(1);
       
        Matcher matcher = pat.matcher(subj);
	System.out.println("MatchStatus : " + matcher.matches());
    }
    
    public static void regexTestBench(List<String> args){
        String reg = args.get(0);
        Pattern pat = Pattern.compile(args.get(0));
        String subj = args.get(1);
        int n = 1000000;
        for(int i=0;i<1000;i++){}
        
        Date d1 = new Date();
        for(int i=0;i<n;i++){
            subj.matches(reg);
        }
        Date d2 = new Date();
        for(int i=0;i<n;i++){
            pat.matcher(subj).matches();
        }
        Date d3 = new Date();
        System.out.println("Result Less is better");
        System.out.println("Bench 1 : " + (d2.getTime() - d1.getTime()));
        System.out.println("Bench 2 : " + (d3.getTime() - d2.getTime()));
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("test")
                .description("Process some test.");
        parser.addArgument("task")
                .metavar("task")
                .type(String.class)
                .choices("regex","regexbench")
                .help("run with specific task");
        parser.addArgument("args")
                .nargs("*")
                .type(String.class)
                .help("args");
        try {
            Namespace res = parser.parseArgs(args);
            switch(res.getString("task")){
                case "regex":
                    regex((List<String>)res.get("args"));
                    break;
                case "regexbench":
                    regexTestBench((List<String>)res.get("args"));
                    break;
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
