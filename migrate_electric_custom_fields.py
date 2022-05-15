#!/usr/bin/env python3

# Migration script for customfield

import requests
import json
import re
import csv

# create one with HOST_URL, API_TOKEN and DEFAULT_FIELD_SET
import my_secrets
# limit for per-request items
FETCH_LIMIT = 200

header_json = {"Content-Type": "application/json", "Accept" : "application/json", "Authorization": "Bearer {}".format(my_secrets.API_TOKEN)}


def set_field_set(model_id):
    url = "{}/api/v1/models/{}".format(my_secrets.HOST_URL,model_id)
    # documentation is wrong, it's custom_fieldset_id
    r = requests.put(url=url,headers=header_json, json={'custom_fieldset_id': my_secrets.DEFAULT_FIELD_SET})
    check_code(r)
    print(r.text)

def get_assets(limit, offset):
    url = "{}/api/v1/hardware?offset={}&limit={}".format(my_secrets.HOST_URL,offset,limit)
    r = requests.request("GET",url,headers=header_json)
    check_code(r)
    return r.json()

def get_all_assets():
    r = get_assets(1,0)
    total = r["total"]
    assets = []
    while (len(assets) < total):
        print("{}/{}".format(len(assets),total))
        r = get_assets(FETCH_LIMIT,len(assets))
        assets += r["rows"]
    return assets

def get_models(limit, offset):
    url = "{}/api/v1/models?offset={}&limit={}".format(my_secrets.HOST_URL,offset,limit)
    r = requests.request("GET",url,headers=header_json)
    check_code(r)
    return r.json()

def get_all_models():
    r = get_models(1,0)
    total = r["total"]
    assets = []
    while (len(assets) < total):
        print("{}/{}".format(len(assets),total))
        r = get_models(FETCH_LIMIT,len(assets))
        assets += r["rows"]
    return assets


def set_asset_field(asset,field_name,id):
    url = "{}/api/v1/hardware/{}".format(my_secrets.HOST_URL,asset)
    r = requests.put(url=url,headers=header_json,json={field_name:id})
    check_code(r)
    print(r.text)

def set_notes(asset,notes):
    url = "{}/api/v1/hardware/{}".format(my_secrets.HOST_URL,asset)
    r = requests.put(url=url,headers=header_json,json={'notes':notes})
    check_code(r)
    print(r.text)

def check_code(response):
    if response.status_code != 200:
        print(response.status_code)
        print(response.text)
        raise RuntimeError("Invalid status code")

def model_definitions():
    with open('model_definitions.csv', mode ='r')as file:
        csvFile = csv.reader(file)
        #return csvFile
        data = {}
        for lines in csvFile:
                data[int(lines[1])] = lines[0]
        return data

model_def = model_definitions()
print(model_def)
assets = get_all_assets()

for m in assets:
    #print(m)
    model_id = m["model"]["id"]
    asset_id = m["id"]
    wahr = "Power supply unit pluggable into wall socket"
    falsch = "Other/None"
    power_supply = m["custom_fields"]["Power Supply"]
    power_val = power_supply["value"]
    #print(json.dumps(power_val))
    # if power_val == "":
    #     print(model_id)
    #     if model_id not in model_def:
    #         print("No default value for model {}".format(model_id))
    #     elif model_def[model_id] == "WAHR" or model_def[model_id] == "FALSCH":
    #         default_val = model_def[model_id]
    #         print("Patching {} from {} to {}".format(asset_id,power_val,model_def[model_id]))
            
    #         wert = wahr
    #         if model_def[model_id] == "FALSCH":#
    #             wert = falsch
    #         # defused
    #         #set_asset_field(asset_id,power_supply["field"],wert)
    if power_val != wahr and power_val != falsch:
        print("Asset {} hat noch wert {}".format(asset_id,json.dumps(power_val)))

        

    

#print(models_without_cfields)



#for model in models_without_cfields:
#    set_field_set(model)


