export interface StringTypes {
    string_id: number;
    name: string;
    sport: string;
    color: string;
    description: string;
    type: string;
    is_active: Boolean;
    created_at: string;
    updated_at: string;
}

export interface StringingOrder {
    order_id: number;
    racket_name: string;
    additional_info: string;
    horizontal_kg: number;
    vertical_kg: number;
    string_id: number;
    status: string;
    created_at: string;
    updated_at: string;
}