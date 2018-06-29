extern crate actix;
extern crate actix_web;
extern crate redis;
extern crate serde;
#[macro_use]
extern crate serde_derive;
#[macro_use]
extern crate serde_json;

use actix::prelude::*;
use actix_web::{server, App, HttpRequest, HttpResponse, Json, Responder};
use actix_web::http::Method;
use actix_web::Result as ActixResult;

struct AppState {
    redis_conn: Addr<redis::Connection>,
}

impl Actor for AppState {
    type Context = SyncContext<Self>;
}

#[derive(Serialize, Deserialize)]
struct TxInfo {
    chain: String,
    expire: Option<u64>,
    transaction: serde_json::Value,
}


#[derive(Serialize, Deserialize)]
struct TxStatus {
    status: String,
    error: Option<String>,
    transaction: serde_json::Value,
}

fn get_tx_info(req: HttpRequest<AppState>) -> impl Responder {
    let uuid = req.match_info().get("uuid").unwrap_or("xxx");
    format!("Hello {}!", uuid)
}

fn post_tx_info(info: Json<TxInfo>) -> impl Responder {
    HttpResponse::Ok().json(info.into_inner())
}

fn get_tx_status(req: HttpRequest<AppState>) -> impl Responder {
    let uuid = req.match_info().get("uuid").unwrap_or("xxx");
    format!("Hello {}!", uuid)
}

fn put_tx_status((req, info): (HttpRequest<AppState>, Json<TxStatus>)) -> impl Responder {
    let uuid = req.match_info().get("uuid").unwrap_or("xxx");
    HttpResponse::Ok().json(info.into_inner())
}

fn main() {
    let redis_conn = redis::Client::open("redis://127.0.0.1/")
        .expect("Connect to redis failed!")
        .get_connection()
        .expect("Get redis connection failed!");

    server::new(|| {
        App::with_state(AppState{ redis_conn })
            .route("/tx/info/", Method::POST, post_tx_info)
            .route("/tx/info/{uuid}", Method::GET, get_tx_info)
            .route("/tx/status/{uuid}", Method::PUT, put_tx_status)
            .route("/tx/status/{uuid}", Method::GET, get_tx_status)
    })
        .bind("0.0.0.0:8000")
        .expect("Can not bind to port 8000")
        .run();
}
