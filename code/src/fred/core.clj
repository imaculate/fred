(ns fred.core
  (:require [fred.image-grabber :as image-grabber]
            [fred.image-saver :as image-saver]
            [fred.image-resizer :as image-resizer]
            [seesaw.dnd :as dnd])
  (:use seesaw.core)
  (:gen-class))

(defn get-parent-path
  "Returns the folder immediately above of the given one."
  [path]
  (str 
      (clojure.string/join  "/"
                            (butlast (clojure.string/split path #"/")))
      "/"))

(defn resize
  "Resizes all images on the given path."
  [path-images use-ldpi?]
  (let [path-folders (get-parent-path path-images)]
    (image-saver/copy-to-dir-and-rename
      (image-grabber/get-images path-images) path-images (str path-folders "drawable-xxhdpi/"))

    (image-saver/clone-dir
      path-folders "drawable-xxhdpi" "drawable-xhdpi" "drawable-hdpi" "drawable-mdpi")

    (image-resizer/resize-dir path-folders "drawable-xhdpi" 8/12)
    (image-resizer/resize-dir path-folders "drawable-hdpi" 6/12)
    (image-resizer/resize-dir path-folders "drawable-mdpi" 4/12)

  (when (= use-ldpi? true)
    (image-saver/clone-dir path-folders "drawable-xxhdpi" "drawable-ldpi")
    (image-resizer/resize-dir path-folders "drawable-ldpi" 3/12)))

  (alert "Your images were resized, buddy. Hope I've been useful.\nDo you want to grab a beer someday?"))

(defn handle-file-drop
  "Grabs the dropped folder and start the resizing process."
  [data use-ldpi?]
  ;; data needs to be one folder.
  ;; TODO: support dropping multiple files.
  ;; TODO: handle exceptions
  (resize (str (.get data 0) "/") use-ldpi?))

(defn -main []
  (native!)
  (def cb (checkbox :text "Create \"drawable-ldpi\"? Sure, I can do that!"))
  (invoke-later
    (-> (frame  :title    "Fred"
                :content  (flow-panel
                            :size [512 :by 630]
                            :items  [(label :icon (clojure.java.io/resource "icon.png")
                                      :drag-enabled? true
                                      :transfer-handler 
                                        (dnd/default-transfer-handler
                                          :import [dnd/file-list-flavor (fn [{:keys [data]}] (handle-file-drop data (value cb)))]))
                                    (text :multi-line? true
                                          :editable? false
                                          :margin 20
                                          :font "BOLD-14"
                                          :text "Hely, pal, how is it going? I can resize images for you, it's no trouble\nat all! Just drag a folder and drop inside me and I'll gladly do the work.")
                                    cb])
                :on-close :exit)
      pack!
      show!)))
