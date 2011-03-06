package com.threerings.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.CommandlineJava;

public class JavaNailgunTask extends Java
{
    /**
     * The output file to use if ngwrite is set..
     */
    public void setNgDest (File dest)
    {
        _ngDest = dest;
    }

    /**
     * If an sh file to invoke nailgun should be written. If this is set to true, ngdest must be
     * set. If a file is written, neither nailgun nor java will run the process directly.
     */
    public void setNgWrite (boolean write)
    {
        _writeNg = Ternary.convert(write);
    }

    /**
     * If nailgun should be run in-process in place of java. This will only occur if ngwrite isn't
     * set.
     */
    public void setNgRun (boolean run)
    {
        _runNg = Ternary.convert(run);
    }

    protected int executeJava (CommandlineJava commandLine)
    {
        if (_writeNg == Ternary.TRUE) {
            try {
                writeCommand();
            } catch (IOException e) {
                throw new BuildException(e);
            }
            return 0;
        } else {
            if (_runNg == Ternary.TRUE) {
                // This gets us to always split off a new command, and spawns if that's set.
                setFork(true);
                return super.executeJava(getDoctoredCommandLine());
            } else {
                return super.executeJava(commandLine);
            }
        }
    }

    protected CommandlineJava getDoctoredCommandLine ()
    {
        return new CommandlineJava() {
            public String[] getCommandline () {
                String[] cmds = JavaNailgunTask.super.getCommandLine().getCommandline();
                List<String> mod = new ArrayList<String>(cmds.length);
                mod.add("ng");
                int ii = 1;// Skip the Java invocation, we replaced it with ng
                for (; ii < cmds.length && cmds[ii].startsWith("-"); ii++) {
                    log("Dropping JVM arg '" + cmds[ii]
                        + "'. Set it on ngserver directly if needed.", Project.MSG_INFO);
                    if (cmds[ii].equals("-classpath") || cmds[ii].equals("-cp")) {
                        ii++;
                    }

                }
                if (ii == cmds.length) {
                    log("Didn't find main class for nailgun. I don't think this is going to turn out well.",
                        Project.MSG_WARN);
                }
                for (; ii < cmds.length; ii++) {
                    mod.add(cmds[ii]);
                }
                return mod.toArray(new String[mod.size()]);

            };
        };
    }

    protected void writeCommand ()
        throws IOException
    {
        if (_ngDest == null) {
            throw new BuildException("ngdest must be set if ngwrite is set");
        }
        FileOutputStream out = new FileOutputStream(_ngDest);
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
        String[] cmds = getDoctoredCommandLine().getCommandline();
        boolean firstLine = true;
        for (String cmd : cmds) {
            if (!firstLine) {
                writer.write(" \\\n  ");
            }
            firstLine = false;
            writer.write(cmd);
        }
        writer.write('\n');
        writer.close();
        log("Wrote nailgun script to " + _ngDest, Project.MSG_INFO);
    }

    enum Ternary {
        UNSET, TRUE, FALSE;

        static Ternary convert (boolean value)
        {
            return value ? TRUE : FALSE;
        }
    }

    protected File _ngDest;
    protected Ternary _writeNg = Ternary.UNSET, _runNg = Ternary.UNSET;
}
