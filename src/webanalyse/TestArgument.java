/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webanalyse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;



/**
 *
 * @author wiwat
 */
public class TestArgument {

    private static interface Accumulate {
        int accumulate(Collection<Integer> ints);
    }

    private static class Sum implements Accumulate {
        @Override
        public int accumulate(Collection<Integer> ints) {
            int sum = 0;
            for (Integer i : ints) {
                sum += i;
            }
            return sum;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private static class Max implements Accumulate {
        @Override
        public int accumulate(Collection<Integer> ints) {
            return Collections.max(ints);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
    
    
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("prog")
                .description("Process some integers.");
        parser.addArgument("integers")
                .metavar("N")
                .type(Integer.class)
                .nargs("+")
                .help("an integer for the accumulator");
        parser.addArgument("--sum")
                .dest("accumulate")
                .action(Arguments.storeConst())
                .setConst(new Sum())
                .setDefault(new Max())
                .help("sum the integers (default: find the max)");
        try {
            Namespace res = parser.parseArgs(args);
            System.out.println(((Accumulate) res.get("accumulate"))
                    .accumulate((List<Integer>) res.get("integers")));
            
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
