#!/usr/bin/env python3
# coding: utf-8

import json
import uuid

from flask import Flask, request, make_response
import redis

app = Flask(__name__)
redis_conn = redis.StrictRedis()


def bad_request(message):
    return make_response(json.dumps({'error': message}), 400)


@app.route('/tx/info/', methods=['POST'])
def add_tx_info():
    data = json.loads(request.data)
    uuid_hex = uuid.uuid4().hex
    chain = data.get('chain')
    if not chain:
        return bad_request('Invalid chain type')
    expire = int(data.get('expire') or 3600)
    if expire > 3600:
        expire = 3600
    transaction = data.get('transaction')
    if not isinstance(transaction, dict):
        return bad_request('Invalid transaction data')

    data['uuid'] = uuid_hex
    data['expire'] = expire

    redis_key_info = 'tx:info:{uuid}'.format(uuid=uuid_hex)
    redis_key_status = 'tx:status:{uuid}'.format(uuid=uuid_hex)
    redis_value_info = json.dumps(data)
    redis_value_status = json.dumps({'uuid': uuid_hex, 'status': 'pending'})
    redis_conn.set(redis_key_info, redis_value_info, ex=expire)
    redis_conn.set(redis_key_status, redis_value_status, ex=expire)
    return json.dumps({'uuid': uuid_hex})


@app.route('/tx/info/<uuid>', methods=['GET'])
def get_tx_info(uuid):
    redis_key = 'tx:info:{uuid}'.format(uuid=uuid)
    redis_value = redis_conn.get(redis_key)
    return redis_value or (404, 'not found')


@app.route('/tx/status/<uuid>', methods=['PUT'])
def update_tx_status(uuid):
    data = json.loads(request.data)
    status = data.get('status')
    error = data.get('error')
    transaction = data.get('transaction')
    if status == 'success':
        if not isinstance(transaction, dict):
            return bad_request('Invalid transaction data(tx_hash)')
    elif status in ['denied', 'faied']:
        if not error:
            return bad_request('Error message is required!')
    else:
        return bad_request('Invalid status')

    redis_key = 'tx:status:{uuid}'.format(uuid=uuid)
    redis_value = json.dumps(data)
    redis_conn.set(redis_key, redis_value)
    return json.dumps({'uuid': uuid})


@app.route('/tx/status/<uuid>', methods=['GET'])
def get_tx_status(uuid):
    redis_key = 'tx:status:{uuid}'.format(uuid=uuid)
    redis_value = redis_conn.get(redis_key)
    return redis_value or (404, 'not found')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000)
