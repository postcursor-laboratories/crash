import os
import sys

CURRENT_SCRIPT_DIR = "${0%/*}"

def rewrite_classpath(cp):
    parts = cp.split(":")
    new_parts = []
    for p in parts:
        # classpath in resources/java
        if 'bin' in p:
            # special case bin is the jar
            new_parts.append(CURRENT_SCRIPT_DIR + "/../Resources/Java/app.jar")
            continue
        new_parts.append(CURRENT_SCRIPT_DIR + "/../Resources/Java/" + p)
    return ":".join(new_parts)

def make_executable(path):
    os.chmod(path, int("777", 8))
    
def main():
    symbol_map = {
        "@CLASSPATH@": rewrite_classpath(sys.argv[1]),
        "@LAUNCH_TARGET@": sys.argv[2],
        "@PROGRAM_ARGS@": '"$@"'
    }
    launchtxt = None
    with open("res/launch.sh", "r") as f:
        launchtxt = f.read()
    for sym, repl in symbol_map.items():
        launchtxt = launchtxt.replace(sym, repl)
    jas = "Crash.app/Contents/MacOS/JavaApplicationStub"
    with open(jas, "w+") as f:
        f.write(launchtxt)
    make_executable(jas)
    
if __name__ == "__main__":
    main()
