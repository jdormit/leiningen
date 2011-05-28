(ns leiningen.util.paths
  (:use [clojure.java.io :only [file]]))

(defn ^{:internal true} normalize-path [project-root path]
  (when path
    (let [f (file path)]
      (.getAbsolutePath (if (.isAbsolute f) f (file project-root path))))))

(defn- get-by-pattern
  "Gets a value from map m, but uses the keys as regex patterns, trying
   to match against k instead of doing an exact match."
  [m k]
  (m (first (drop-while #(nil? (re-find (re-pattern %) k))
                        (keys m)))))

(def ^{:private true} native-names
  {"Mac OS X" :macosx "Windows" :windows "Linux" :linux
   "FreeBSD" :freebsd "OpenBSD" :openbsd
   "amd64" :x86_64 "x86_64" :x86_64 "x86" :x86 "i386" :x86
   "arm" :arm "SunOS" :solaris "sparc" :sparc})

(defn get-os
  "Returns a keyword naming the host OS."
  []
  (get-by-pattern native-names (System/getProperty "os.name")))

(defn get-arch
  "Returns a keyword naming the host architecture"
  []
  (get-by-pattern native-names (System/getProperty "os.arch")))

(defn native-path
  "Returns a File representing the directory where native libs for the
  current platform are located."
  [project]
  (when (and (get-os) (get-arch))
    (let [osdir (name (get-os))
          archdir (name (get-arch))
          f (file "native" osdir archdir)]
      (if (.exists f)
        f
        nil))))

(defn leiningen-home
  "Returns full path to Lein home dir ($LEIN_HOME or $HOME/.lein) if it exists"
  []
  (.getAbsolutePath (doto (if-let [lein-home (System/getenv "LEIN_HOME")]
                            (file lein-home)
                            (file (System/getProperty "user.home") ".lein"))
                      .mkdirs)))

(defn ns->path [n]
  (str (.. (str n)
           (replace \- \_)
           (replace \. \/))
       ".clj"))

(defn path->ns [path]
  (.. (.replaceAll path "\\.clj" "")
      (replace \_ \-)
      (replace \/ \.)))

