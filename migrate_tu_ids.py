#!/usr/bin/env python3

# Migration script for moving tu-inventory-numbers from asset notes to a specific custom field

import requests
import json
import re

# create one with HOST_URL, API_TOKEN and DEFAULT_FIELD_SET
import secrets
TU_ID_FIELD='TU Inventory Number'

regex = re.compile("((\d\s?){6}\s?-\s?(\d\s?){6})")

header_json = {"Content-Type": "application/json", "Accept" : "application/json", "Authorization": "Bearer {}".format(secrets.API_TOKEN)}


def set_field_set(model_id):
    url = "{}/api/v1/models/{}".format(secrets.HOST_URL,model_id)
    # documentation is wrong, it's custom_fieldset_id
    r = requests.put(url=url,headers=header_json, json={'custom_fieldset_id': secrets.DEFAULT_FIELD_SET})
    assert r.status_code == 200
    print(r.text)

def get_entries(limit, offset):
    url = "{}/api/v1/hardware?offset={}&limit={}".format(secrets.HOST_URL,offset,limit)
    r = requests.request("GET",url,headers=header_json)
    assert r.status_code == 200
    return r.json()

def get_all_entries():
    r = get_entries(1,0)
    total = r["total"]
    limit=200
    assets = []
    while (len(assets) < total):
        print("{}/{}".format(len(assets),total))
        r = get_entries(limit,len(assets))
        assets += r["rows"]
    return assets

def set_tu_id(asset,field_name,id):
    url = "{}/api/v1/hardware/{}".format(secrets.HOST_URL,asset)
    r = requests.put(url=url,headers=header_json,json={field_name:id})
    assert r.status_code == 200
    print(r.text)

def set_notes(asset,notes):
    url = "{}/api/v1/hardware/{}".format(secrets.HOST_URL,asset)
    r = requests.put(url=url,headers=header_json,json={'notes':notes})
    assert r.status_code == 200
    print(r.text)

entries = get_all_entries()

models_without_cfields = set()

for item in entries:
    if item["notes"] is not None and item["notes"].strip():
        #print('{}:"{}"'.format(item["id"],item["notes"]))
        res = regex.search(item["notes"])
        if res is not None:
            tu_id = res.group(1).replace(" ","").strip()
            print('Match {}:"{}"'.format(item["id"],tu_id))
            if item["custom_fields"] is None or len(item["custom_fields"]) == 0 or item["custom_fields"][TU_ID_FIELD] is None:
                models_without_cfields.add(item["model"]["id"])
        else:
            print('No Match {}:"{}"'.format(item["id"],item["notes"]))

print(models_without_cfields)

for model in models_without_cfields:
    set_field_set(model)

def update_fields(entries):
    for item in entries:
        if item["notes"] is not None and item["notes"].strip():
            #print('{}:"{}"'.format(item["id"],item["notes"]))
            res = regex.search(item["notes"])
            if res is not None:
                tu_id = res.group(1).replace(" ","").strip()
                print(item["custom_fields"])
                for field in item["custom_fields"]:
                    if field == TU_ID_FIELD:
                        field_name = item["custom_fields"][field]["field"]
                        current_value = item["custom_fields"][field]["value"]
                        if current_value is not None and current_value.strip() != "" and tu_id != current_value:
                            print("Diverging: {} from {}".format(tu_id,current_value))
                        #defused
                        #else:
                            #set_tu_id(item["id"],field_name,tu_id)

#update_fields(entries)

def remove_id_from_notes(entries):
    for item in entries:
        if item["notes"] is not None and item["notes"].strip():
            for field in item["custom_fields"]:
                if field == TU_ID_FIELD:
                    current_value = item["custom_fields"][field]["value"]
                    if current_value is not None and current_value.strip():
                        notes_cleared = item["notes"].replace(current_value,"").strip()
                        if notes_cleared == "":
                            notes_cleared = None
                        # defused
                        #set_notes(item["id"],notes_cleared)
                        print(notes_cleared)
                        break

remove_id_from_notes(entries)