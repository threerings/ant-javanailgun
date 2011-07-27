The javanailgun task is a drop-in replacement for the java task with a few extra attributes: ngwrite, ngrun, and ngdest. If none of these are set, it functions identically to the java task. If ngrun is set to true, it runs the given class using ng via exec. Any VM arguments are dropped, but regular arguments are passed through. If ngwrite is set to true, a script is written to the value in ngdest. If either ngwrite and ngrun are true, the Java invocation is set. If both ngwrite and ngrun are true, only ngwrite is executed.

The task is defined in `com/threerings/ant/javanailgun/antlib.xml` in the javanailgun jar. To use it, add its jar to a classpath in ant and run a taskdef:
```xml
<path id="javanailgun.path">
  <fileset dir="lib" includes="javanailgun.jar"/>
</path>
<taskdef resource="com/threerings/ant/javanailgun/antlib.xml" classpathref="javanailgun.path"/>
```


After that, you can use the `javanailgun` task.

Alternatively, if you're using [ooo-build](http://code.google.com/p/ooo-build/), you can use `<maventaskdef groupId="com.threerings.ant" artifactId="javanailgun" version="1.0"/>` to load it.

