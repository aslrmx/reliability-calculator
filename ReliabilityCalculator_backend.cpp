// C++ program to compute availability/MTBF for components in series/parallel

#include <iostream>
#include <string>
#include <vector>
using namespace std;
#include "httplib.h"

struct Component {
    string name;
    double mtbf;
    double availability;
};

// series calculation
double calculateSeries(vector<Component> components) {
    double total = 1;
    for (int i = 0; i < components.size(); i++) {
        total = total * (components[i].availability / 100);
    }
    return total * 100;
}

// parallel calculation
double calculateParallel(vector<Component> components) {
    double total = 1;
    for (int i = 0; i < components.size(); i++) {
        double temp = 1 - (components[i].availability / 100);
        total = total * temp;
    }
    return (1 - total) * 100;
}

// simple json parsing
void parseJSON(string json, string &config, vector<Component> &components) {
    size_t pos = json.find("\"configuration\":\"");
    if (pos != string::npos) {
        pos = pos + 17;
        size_t end = json.find("\"", pos);
        config = json.substr(pos, end - pos);
    }
    
    size_t start = json.find("\"components\":[");
    if (start == string::npos) return;
    
    start = start + 14;
    size_t endArray = json.find("]", start);
    string array = json.substr(start, endArray - start);
    
    size_t current = 0;
    while (true) {
        size_t open = array.find("{", current);
        if (open == string::npos) break;
        
        size_t close = array.find("}", open);
        string obj = array.substr(open, close - open + 1);
        
        Component c;
        
        size_t namePos = obj.find("\"name\":\"");
        if (namePos != string::npos) {
            namePos = namePos + 8;
            size_t nameEnd = obj.find("\"", namePos);
            c.name = obj.substr(namePos, nameEnd - namePos);
        }
        
        size_t mtbfPos = obj.find("\"mtbf\":");
        if (mtbfPos != string::npos) {
            mtbfPos = mtbfPos + 7;
            size_t mtbfEnd = obj.find_first_of(",}", mtbfPos);
            c.mtbf = stod(obj.substr(mtbfPos, mtbfEnd - mtbfPos));
        }
        
        size_t availPos = obj.find("\"availability\":");
        if (availPos != string::npos) {
            availPos = availPos + 15;
            size_t availEnd = obj.find_first_of(",}", availPos);
            c.availability = stod(obj.substr(availPos, availEnd - availPos));
        }
        
        components.push_back(c);
        current = close + 1;
    }
}

string createResult(string config, vector<Component> components) {
    string result = "";
    result += "Configuration: " + config + "\n";
    result += "Number of components: " + to_string(components.size()) + "\n\n";
    
    for (int i = 0; i < components.size(); i++) {
        result += components[i].name + "\n";
        result += "MTBF: " + to_string(components[i].mtbf) + "\n";
        result += "Availability: " + to_string(components[i].availability) + "%\n\n";
    }
    
    double systemAvail = 0;
    if (config == "series") {
        systemAvail = calculateSeries(components);
        result += "Series system\n";
    }
    else if (config == "parallel") {
        systemAvail = calculateParallel(components);
        result += "Parallel system\n";
    }
    
    result += "System Availability: " + to_string(systemAvail) + "%\n";
    
    double uptime = (systemAvail / 100) * 8760;
    double downtime = 8760 - uptime;
    
    result += "Uptime per year: " + to_string(uptime) + "\n";
    result += "Downtime per year: " + to_string(downtime) + "\n";
    
    return result;
}

int main() {
    httplib::Server server;
    cout << "Server running on port 8080\n";
    
    server.Post("/calculate", [](const httplib::Request& req, httplib::Response& res) {
        string json = req.body;
        string config;
        vector<Component> components;
        
        parseJSON(json, config, components);
        string result = createResult(config, components);
        
        res.set_content(result, "text/plain");
    });
    
    server.listen("127.0.0.1", 8080);
    return 0;
}
