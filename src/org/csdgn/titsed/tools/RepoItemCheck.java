/**
 * Copyright (c) 2020 Robert Maupin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.csdgn.titsed.tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.csdgn.maru.Streams;
import org.csdgn.titsed.model.DataModel;
import org.csdgn.titsed.model.ItemEntry;

public class RepoItemCheck {
    // https://api.github.com/repos/OXOIndustries/TiTS-Public/contents/classes/Items

    static String parseEntry(String data, int index) {
        int start = data.indexOf("\"", index);
        int end = data.indexOf("\"", start + 1);
        return data.substring(start + 1, end);
    }

    static List<String> readItemsFromGitHub(String urlString) throws IOException {
        List<String> items = new ArrayList<String>();
        URL url = new URL(urlString);
        String data = new String(Streams.getAndClose(url.openStream()), StandardCharsets.UTF_8);

        int index = 0;
        while ((index = data.indexOf("\"path\"", index + 6)) != -1) {
            String path = parseEntry(data, index + 6);
            if (path.endsWith(".as")) {
                //System.out.println(path);
                int cut = path.lastIndexOf('/');
                path = path.substring(0, cut).replace('/', '.') + "::" + path.substring(cut + 1, path.length() - 3);
                items.add(path);
            } else {
                String subDir = parseEntry(data, data.indexOf("\"url\"", index + 6) + 5);
                items.addAll(readItemsFromGitHub(subDir));
            }
        }
        return items;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Loading data from web.");
        List<String> gitems = readItemsFromGitHub("https://api.github.com/repos/OXOIndustries/TiTS-Public/contents/classes/Items");

        System.out.println("Loading data from xmls.");
		final DataModel dm = new DataModel();
        dm.load();
        
        for(ItemEntry item : dm.getItemList()) {
            //if we have an item that is not in the list
            if(!gitems.contains(item.id)) {
                System.out.println("Xml contains invalid id: " + item.id);
            } else {
                gitems.remove(item.id);
            }
        }

        for(String item : gitems) {
            System.out.println("Web contains missing id: " + item);
        }
    }
}